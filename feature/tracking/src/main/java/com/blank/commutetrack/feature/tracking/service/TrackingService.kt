package com.blank.commutetrack.feature.tracking.service

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import com.blank.commutetrack.core.domain.model.SessionStatus
import com.blank.commutetrack.core.domain.usecase.*
import com.blank.commutetrack.feature.tracking.location.LocationTrackingManager
import com.blank.commutetrack.feature.tracking.notification.TrackingNotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

/**
 * Foreground service for tracking commute trips.
 * Shows persistent notification and tracks location in background.
 */
@AndroidEntryPoint
class TrackingService : Service() {

    @Inject
    lateinit var getActiveSession: GetActiveSessionUseCase
    
    @Inject
    lateinit var pauseSession: PauseSessionUseCase
    
    @Inject
    lateinit var resumeSession: ResumeSessionUseCase
    
    @Inject
    lateinit var endSession: EndSessionUseCase

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private lateinit var locationManager: LocationTrackingManager
    
    // Service state
    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()
    
    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()
    
    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds.asStateFlow()
    
    private val _totalDistance = MutableStateFlow(0.0)
    val totalDistance: StateFlow<Double> = _totalDistance.asStateFlow()
    
    private var timerJob: Job? = null
    private var activeSessionId: Long? = null
    private var startTime: kotlinx.datetime.Instant? = null
    private var pausedDurationSeconds: Long = 0
    private var pauseStartTime: kotlinx.datetime.Instant? = null
    private var startLocationName: String = ""
    private var pauseCount: Int = 0

    inner class LocalBinder : Binder() {
        fun getService(): TrackingService = this@TrackingService
    }

    override fun onCreate() {
        super.onCreate()
        locationManager = LocationTrackingManager(this)
        TrackingNotificationHelper.createNotificationChannel(this)
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            TrackingNotificationHelper.ACTION_PAUSE -> handlePause()
            TrackingNotificationHelper.ACTION_RESUME -> handleResume()
            TrackingNotificationHelper.ACTION_STOP -> handleStop()
            else -> {
                // Service started for the first time
                startForegroundTracking()
            }
        }
        return START_STICKY
    }

    private fun startForegroundTracking() {
        // Start as foreground service immediately with initial notification
        val notification = TrackingNotificationHelper.buildNotification(
            context = this,
            elapsedTime = formatElapsedTime(0),
            startLocation = "Locating...",
            isPaused = false,
            pauseCount = 0
        )
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                TrackingNotificationHelper.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(TrackingNotificationHelper.NOTIFICATION_ID, notification)
        }
        
        // Then collect session data
        serviceScope.launch {
            getActiveSession().collect { session ->
                if (session != null) {
                    activeSessionId = session.id
                    _isTracking.value = true
                    _isPaused.value = session.status == SessionStatus.PAUSED
                    pauseCount = session.pauseCount
                    
                    // Calculate initial elapsed time
                    val now = Clock.System.now()
                    val sessionStart = session.startTime.toInstant(TimeZone.currentSystemDefault())
                    val totalElapsed = now.epochSeconds - sessionStart.epochSeconds
                    pausedDurationSeconds = session.pausedMinutes * 60L
                    _elapsedSeconds.value = (totalElapsed - pausedDurationSeconds).coerceAtLeast(0)
                    startTime = sessionStart
                    startLocationName = session.startLocation
                    
                    if (!_isPaused.value) {
                        startLocationTracking()
                        startTimer()
                    }
                    
                    updateNotification()
                } else {
                    // No active session, stop service
                    stopTrackingService()
                }
            }
        }
    }

    private fun startLocationTracking() {
        locationManager.startTracking()
        
        // Observe location updates
        serviceScope.launch {
            locationManager.totalDistance.collect { distance ->
                _totalDistance.value = distance / 1000.0 // Convert to km
            }
        }
        
        // Update start location name when address is available
        serviceScope.launch {
            locationManager.currentAddress.collect { address ->
                if (address.isNotBlank() && startLocationName.isBlank()) {
                    startLocationName = address
                }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive) {
                delay(1000)
                _elapsedSeconds.value++
                updateNotification()
            }
        }
    }

    private fun handlePause() {
        serviceScope.launch {
            activeSessionId?.let { id ->
                pauseSession(id)
                _isPaused.value = true
                pauseStartTime = Clock.System.now()
                pauseCount++
                
                timerJob?.cancel()
                locationManager.pauseTracking()
                
                updateNotification()
            }
        }
    }

    private fun handleResume() {
        serviceScope.launch {
            activeSessionId?.let { id ->
                // Calculate pause duration
                pauseStartTime?.let { pauseStart ->
                    val pauseEnd = Clock.System.now()
                    val pauseSeconds = pauseEnd.epochSeconds - pauseStart.epochSeconds
                    val pauseMinutes = (pauseSeconds / 60).toInt().coerceAtLeast(0)
                    resumeSession(id, pauseMinutes)
                    pausedDurationSeconds += pauseSeconds
                } ?: resumeSession(id, 0)
                
                _isPaused.value = false
                pauseStartTime = null
                
                locationManager.resumeTracking()
                startTimer()
                updateNotification()
            }
        }
    }

    private fun handleStop() {
        serviceScope.launch {
            activeSessionId?.let { id ->
                // If paused, account for final pause duration
                if (_isPaused.value) {
                    pauseStartTime?.let { pauseStart ->
                        val pauseEnd = Clock.System.now()
                        val pauseSeconds = pauseEnd.epochSeconds - pauseStart.epochSeconds
                        val pauseMinutes = (pauseSeconds / 60).toInt().coerceAtLeast(0)
                        resumeSession(id, pauseMinutes)
                    }
                }
                
                // Get end location name
                val endLocationName = locationManager.getCurrentLocationName()
                
                // End the session with calculated distance
                val distanceKm = _totalDistance.value
                endSession(id, endLocationName, distanceKm)
                
                stopTrackingService()
            }
        }
    }

    private fun stopTrackingService() {
        timerJob?.cancel()
        locationManager.stopTracking()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun updateNotification() {
        val locationText = if (startLocationName.isNotBlank()) {
            startLocationName
        } else {
            locationManager.getCurrentCoordinates()
        }
        
        val notification = TrackingNotificationHelper.buildNotification(
            context = this,
            elapsedTime = formatElapsedTime(_elapsedSeconds.value),
            startLocation = locationText,
            isPaused = _isPaused.value,
            pauseCount = pauseCount
        )
        
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(TrackingNotificationHelper.NOTIFICATION_ID, notification)
    }

    private fun formatElapsedTime(totalSeconds: Long): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        
        return if (hours > 0) {
            "%02d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%02d:%02d".format(minutes, seconds)
        }
    }

    /**
     * Get the current address for the end location.
     */
    suspend fun getCurrentAddress(): String {
        return locationManager.getCurrentLocationName()
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        locationManager.stopTracking()
        serviceScope.cancel()
    }
}

// Extension to convert LocalDateTime to Instant for calculations
// Using kotlinx.datetime's built-in toInstant function
