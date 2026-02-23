package com.blank.commutetrack.feature.tracking.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Manages location tracking for commute sessions.
 * Uses FusedLocationProvider for battery-efficient location updates.
 */
class LocationTrackingManager(context: Context) {
    
    private val fusedLocationClient: FusedLocationProviderClient = 
        LocationServices.getFusedLocationProviderClient(context)
    
    private val addressLookupHelper = AddressLookupHelper(context)
    
    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()
    
    private val _totalDistance = MutableStateFlow(0.0)
    val totalDistance: StateFlow<Double> = _totalDistance.asStateFlow()
    
    private val _currentAddress = MutableStateFlow<String>("")
    val currentAddress: StateFlow<String> = _currentAddress.asStateFlow()
    
    private var lastLocation: Location? = null
    private var isTracking = false
    
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                updateLocation(location)
            }
        }
    }
    
    @SuppressLint("MissingPermission")
    fun startTracking() {
        if (isTracking) return
        isTracking = true
        
        // Reset distance when starting fresh
        _totalDistance.value = 0.0
        lastLocation = null
        
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            5000L // Update every 5 seconds
        ).apply {
            setMinUpdateIntervalMillis(3000L) // Minimum 3 seconds between updates
            setWaitForAccurateLocation(false)
        }.build()
        
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            
            // Get initial location
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let { 
                    _currentLocation.value = it
                    lastLocation = it
                    // Update address
                    CoroutineScope(Dispatchers.Main).launch {
                        _currentAddress.value = addressLookupHelper.getAddressFromLocation(it)
                    }
                }
            }
        } catch (e: SecurityException) {
            // Permission not granted - handle in UI
            isTracking = false
        }
    }
    
    fun stopTracking() {
        if (!isTracking) return
        isTracking = false
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
    
    fun pauseTracking() {
        // Keep location updates but don't accumulate distance
        isTracking = false
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
    
    @SuppressLint("MissingPermission")
    fun resumeTracking() {
        if (isTracking) return
        isTracking = true
        lastLocation = _currentLocation.value // Reset last location to avoid distance jump
        
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            5000L
        ).apply {
            setMinUpdateIntervalMillis(3000L)
        }.build()
        
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            isTracking = false
        }
    }
    
    private fun updateLocation(newLocation: Location) {
        _currentLocation.value = newLocation
        
        // Update address asynchronously
        CoroutineScope(Dispatchers.Main).launch {
            _currentAddress.value = addressLookupHelper.getAddressFromLocation(newLocation)
        }
        
        lastLocation?.let { last ->
            // Calculate distance from last point
            val distance = calculateDistance(
                last.latitude, last.longitude,
                newLocation.latitude, newLocation.longitude
            )
            
            // Only add if distance is reasonable (>5m to avoid GPS noise, <1000m to avoid jumps)
            if (distance in 5.0..1000.0) {
                _totalDistance.value += distance
            }
        }
        
        lastLocation = newLocation
    }
    
    /**
     * Get the current location as a readable address string.
     */
    suspend fun getCurrentLocationName(): String {
        return addressLookupHelper.getAddressFromLocation(_currentLocation.value)
    }
    
    /**
     * Get a short version of current location.
     */
    suspend fun getShortLocationName(): String {
        return addressLookupHelper.getShortLocationName(_currentLocation.value)
    }
    
    /**
     * Get current coordinates as string.
     */
    fun getCurrentCoordinates(): String {
        val loc = _currentLocation.value
        return if (loc != null) {
            "${loc.latitude.format(4)}, ${loc.longitude.format(4)}"
        } else {
            "Unknown Location"
        }
    }
    
    /**
     * Get the last known location quickly (may be null).
     */
    fun getLastLocation(): Location? = _currentLocation.value
    
    /**
     * Calculate distance between two coordinates using Haversine formula.
     * Returns distance in meters.
     */
    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val R = 6371000.0 // Earth's radius in meters
        
        val latRad1 = Math.toRadians(lat1)
        val latRad2 = Math.toRadians(lat2)
        val deltaLat = Math.toRadians(lat2 - lat1)
        val deltaLon = Math.toRadians(lon2 - lon1)
        
        val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
                cos(latRad1) * cos(latRad2) *
                sin(deltaLon / 2) * sin(deltaLon / 2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return R * c
    }
    
    private fun Double.format(decimals: Int) = "%.${decimals}f".format(this)
    
    /**
     * Reset distance counter (useful when resuming from pause)
     */
    fun resetDistance() {
        _totalDistance.value = 0.0
    }
    
    companion object {
        const val MIN_DISTANCE_THRESHOLD = 5.0 // meters
        const val MAX_DISTANCE_THRESHOLD = 1000.0 // meters - avoid GPS jumps
    }
}
