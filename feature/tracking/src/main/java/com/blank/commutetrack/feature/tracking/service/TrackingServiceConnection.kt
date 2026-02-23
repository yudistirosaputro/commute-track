package com.blank.commutetrack.feature.tracking.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages connection to the TrackingService.
 * Handles binding/unbinding and exposes service state to the UI.
 */
class TrackingServiceConnection {
    
    private var trackingService: TrackingService? = null
    private var isBound = false
    private var context: Context? = null
    
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TrackingService.LocalBinder
            trackingService = binder.getService()
            _isConnected.value = true
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            trackingService = null
            _isConnected.value = false
        }
    }
    
    fun bindService(context: Context) {
        if (isBound) return
        
        this.context = context.applicationContext
        val intent = Intent(context, TrackingService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        isBound = true
    }
    
    fun unbindService(context: Context) {
        if (!isBound) return
        
        try {
            context.unbindService(serviceConnection)
        } catch (e: Exception) {
            // Ignore if already unbound
        }
        isBound = false
        _isConnected.value = false
        trackingService = null
    }
    
    fun startService(context: Context) {
        this.context = context.applicationContext
        val intent = Intent(context, TrackingService::class.java)
        context.startForegroundService(intent)
        bindService(context)
    }
    
    fun stopService(context: Context? = null) {
        val safeContext = context ?: this.context
        safeContext?.let { ctx ->
            try {
                if (isBound) {
                    ctx.unbindService(serviceConnection)
                }
            } catch (e: Exception) {
                // Ignore if already unbound
            }
            val intent = Intent(ctx, TrackingService::class.java)
            ctx.stopService(intent)
        }
        isBound = false
        _isConnected.value = false
        trackingService = null
    }
    
    fun getService(): TrackingService? = trackingService
    
    fun isTracking(): Boolean = trackingService?.isTracking?.value ?: false
    fun isPaused(): Boolean = trackingService?.isPaused?.value ?: false
    fun getElapsedSeconds(): Long = trackingService?.elapsedSeconds?.value ?: 0
    fun getTotalDistance(): Double = trackingService?.totalDistance?.value ?: 0.0
}
