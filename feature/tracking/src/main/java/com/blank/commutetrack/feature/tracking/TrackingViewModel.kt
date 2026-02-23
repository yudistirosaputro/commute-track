package com.blank.commutetrack.feature.tracking

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blank.commutetrack.core.domain.model.CommuteSession
import com.blank.commutetrack.core.domain.model.SessionStatus
import com.blank.commutetrack.core.domain.model.TransportMode
import com.blank.commutetrack.core.domain.usecase.EndSessionUseCase
import com.blank.commutetrack.core.domain.usecase.GetActiveSessionUseCase
import com.blank.commutetrack.core.domain.usecase.PauseSessionUseCase
import com.blank.commutetrack.core.domain.usecase.ResumeSessionUseCase
import com.blank.commutetrack.core.domain.usecase.StartSessionUseCase
import com.blank.commutetrack.feature.tracking.location.AddressLookupHelper
import com.blank.commutetrack.feature.tracking.service.TrackingServiceConnection
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class TrackingViewModel @Inject constructor(
    private val getActiveSession: GetActiveSessionUseCase,
    private val startSession: StartSessionUseCase,
    private val endSession: EndSessionUseCase,
    private val pauseSession: PauseSessionUseCase,
    private val resumeSession: ResumeSessionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var pauseStartTime: Instant? = null
    private val serviceConnection = TrackingServiceConnection()
    private var addressLookupHelper: AddressLookupHelper? = null
    private var appContext: Context? = null

    init {
        loadActiveSession()
    }

    private fun loadActiveSession() {
        viewModelScope.launch {
            getActiveSession().collect { session ->
                val isPaused = session?.status == SessionStatus.PAUSED
                
                // Calculate elapsed seconds
                val elapsedSeconds = if (session != null) {
                    calculateElapsedSeconds(session, isPaused)
                } else {
                    0L
                }
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        activeSession = session,
                        isTracking = session != null,
                        isPaused = isPaused,
                        startLocation = session?.startLocation ?: it.startLocation,
                        selectedTransportMode = session?.transportMode ?: it.selectedTransportMode,
                        pauseCount = session?.pauseCount ?: 0,
                        pausedMinutes = session?.pausedMinutes ?: 0,
                        elapsedSeconds = elapsedSeconds
                    )
                }
                
                // Manage timer job for UI updates when service is not bound
                if (session != null && !isPaused) {
                    startTimer()
                } else {
                    timerJob?.cancel()
                }
            }
        }
    }

    private fun calculateElapsedSeconds(session: CommuteSession, isPaused: Boolean): Long {
        val now = Clock.System.now()
        val start = session.startTime.toSystemInstant()
        
        // Calculate total elapsed time including days
        val totalSeconds = now.epochSeconds - start.epochSeconds
        val pausedSeconds = session.pausedMinutes * 60L
        
        return (totalSeconds - pausedSeconds).coerceAtLeast(0L)
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1.seconds)
                // Update from service if connected, otherwise calculate locally
                val serviceSeconds = serviceConnection.getElapsedSeconds()
                if (serviceSeconds > 0) {
                    _uiState.update { it.copy(elapsedSeconds = serviceSeconds) }
                } else {
                    _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
                }
                
                // Update distance from service
                val serviceDistance = serviceConnection.getTotalDistance()
                if (serviceDistance > 0) {
                    _uiState.update { it.copy(calculatedDistanceKm = serviceDistance) }
                }
            }
        }
    }

    /**
     * Initialize the ViewModel with a context for address lookup.
     * Call this when the screen is first created.
     */
    fun initialize(context: Context) {
        if (addressLookupHelper == null) {
            addressLookupHelper = AddressLookupHelper(context)
            appContext = context.applicationContext
        }
    }

    /**
     * Fetch current location and auto-fill start location.
     * Uses getCurrentLocation for fresh location instead of lastLocation.
     */
    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation(context: Context) {
        initialize(context)
        
        viewModelScope.launch {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                
                // Check permission
                val hasPermission = android.content.pm.PackageManager.PERMISSION_GRANTED ==
                    context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                
                if (!hasPermission) return@launch
                
                // Try to get current location first (more accurate than lastLocation)
                val locationRequest = LocationRequest.Builder(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    10000L
                ).build()
                
                // Try getting a fresh location
                try {
                    val currentLocation = fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                        null
                    ).await()
                    
                    if (currentLocation != null) {
                        updateLocationAddress(currentLocation)
                        return@launch
                    }
                } catch (e: Exception) {
                    // Fall through to lastLocation
                }
                
                // Fallback to lastLocation
                val lastLocation = fusedLocationClient.lastLocation.await()
                if (lastLocation != null) {
                    updateLocationAddress(lastLocation)
                }
            } catch (e: Exception) {
                // Ignore errors
            }
        }
    }
    
    private suspend fun updateLocationAddress(location: Location) {
        val address = addressLookupHelper?.getAddressFromLocation(location) 
            ?: "${location.latitude.format(3)}, ${location.longitude.format(3)}"
        
        _uiState.update { state ->
            state.copy(
                autoDetectedLocation = address,
                startLocation = if (state.startLocation.isBlank()) address else state.startLocation
            )
        }
    }

    /**
     * Bind to the tracking service when the screen is visible.
     */
    fun bindService(context: Context) {
        serviceConnection.bindService(context)
        
        // Collect service state updates
        viewModelScope.launch {
            serviceConnection.isConnected.collect { connected ->
                if (connected) {
                    // Sync with service state
                    launch {
                        serviceConnection.getService()?.elapsedSeconds?.collect { seconds ->
                            _uiState.update { it.copy(elapsedSeconds = seconds) }
                        }
                    }
                    launch {
                        serviceConnection.getService()?.totalDistance?.collect { distance ->
                            _uiState.update { it.copy(calculatedDistanceKm = distance) }
                        }
                    }
                }
            }
        }
    }

    /**
     * Unbind from the tracking service when the screen is not visible.
     */
    fun unbindService(context: Context) {
        serviceConnection.unbindService(context)
    }

    fun updateStartLocation(location: String) {
        _uiState.update { it.copy(startLocation = location) }
    }

    fun selectTransportMode(mode: TransportMode) {
        _uiState.update { it.copy(selectedTransportMode = mode) }
    }

    /**
     * Start a new trip and launch the foreground service.
     */
    fun startTrip(context: Context) {
        val location = _uiState.value.startLocation
        if (location.isBlank()) return
        
        viewModelScope.launch {
            // Start the session in database first
            startSession(location, _uiState.value.selectedTransportMode)
            
            // Small delay to ensure session is created before starting service
            delay(300)
            
            // Start the foreground service
            serviceConnection.startService(context)
        }
    }

    fun pauseTrip() {
        viewModelScope.launch {
            _uiState.value.activeSession?.let { session ->
                pauseStartTime = Clock.System.now()
                pauseSession(session.id)
                timerJob?.cancel()
                _uiState.update { it.copy(isPaused = true) }
            }
        }
    }

    fun resumeTrip() {
        viewModelScope.launch {
            _uiState.value.activeSession?.let { session ->
                val pauseEnd = Clock.System.now()
                val pauseStart = pauseStartTime ?: pauseEnd

                // Calculate pause duration in minutes (round up)
                val pauseDurationSeconds = pauseEnd.epochSeconds - pauseStart.epochSeconds
                val pauseDurationMinutes = (pauseDurationSeconds / 60).toInt().coerceAtLeast(0)

                resumeSession(session.id, pauseDurationMinutes)
                pauseStartTime = null

                _uiState.update {
                    it.copy(
                        isPaused = false,
                        pauseCount = it.pauseCount + 1,
                        pausedMinutes = it.pausedMinutes + pauseDurationMinutes
                    )
                }
                
                // Restart timer
                startTimer()
            }
        }
    }

    /**
     * End the current trip and stop the service.
     */
    fun endTrip(endLocation: String, distanceKm: Double) {
        viewModelScope.launch {
            _uiState.value.activeSession?.let { session ->
                // If paused, account for final pause duration
                if (_uiState.value.isPaused) {
                    val pauseEnd = Clock.System.now()
                    val pauseStart = pauseStartTime ?: pauseEnd
                    val pauseDurationSeconds = pauseEnd.epochSeconds - pauseStart.epochSeconds
                    val pauseDurationMinutes = (pauseDurationSeconds / 60).toInt().coerceAtLeast(0)
                    resumeSession(session.id, pauseDurationMinutes)
                    pauseStartTime = null
                }
                
                // Use calculated distance if manual distance is 0
                val finalDistance = if (distanceKm > 0) distanceKm else _uiState.value.calculatedDistanceKm
                val finalEndLocation = endLocation.ifBlank { _uiState.value.autoDetectedLocation }
                endSession(session.id, finalEndLocation, finalDistance)
                
                // Stop the service using stored context
                serviceConnection.stopService(appContext)
            }
        }
    }

    /**
     * Get the current address for end location.
     */
    suspend fun getCurrentEndAddress(context: Context): String {
        return try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            val location = fusedLocationClient.lastLocation.await()
            addressLookupHelper?.getAddressFromLocation(location) ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

// Extension to convert LocalDateTime to Instant for calculations
private fun kotlinx.datetime.LocalDateTime.toSystemInstant(): Instant {
    return this.toInstant(TimeZone.currentSystemDefault())
}

// Helper extension for Double formatting
private fun Double.format(decimals: Int) = "%.${decimals}f".format(this)
