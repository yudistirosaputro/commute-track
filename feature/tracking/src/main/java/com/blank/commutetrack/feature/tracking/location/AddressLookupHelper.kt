package com.blank.commutetrack.feature.tracking.location

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

/**
 * Helper class for converting GPS coordinates to human-readable addresses.
 * Uses Android's Geocoder for reverse geocoding.
 */
class AddressLookupHelper(private val context: Context) {
    
    private val geocoder = Geocoder(context, Locale.getDefault())
    
    /**
     * Get a readable address from a Location object.
     * Returns a simplified address (street name, neighborhood, or city).
     */
    suspend fun getAddressFromLocation(location: Location?): String = withContext(Dispatchers.IO) {
        if (location == null) return@withContext "Unknown Location"
        
        try {
            val address = getAddress(location.latitude, location.longitude)
            address?.let { formatAddress(it) } ?: "${location.latitude.format(3)}, ${location.longitude.format(3)}"
        } catch (e: Exception) {
            // Fallback to coordinates if geocoding fails
            "${location.latitude.format(3)}, ${location.longitude.format(3)}"
        }
    }
    
    /**
     * Get a readable address from latitude and longitude.
     */
    suspend fun getAddressFromCoordinates(latitude: Double, longitude: Double): String = withContext(Dispatchers.IO) {
        try {
            val address = getAddress(latitude, longitude)
            address?.let { formatAddress(it) } ?: "${latitude.format(3)}, ${longitude.format(3)}"
        } catch (e: Exception) {
            "${latitude.format(3)}, ${longitude.format(3)}"
        }
    }
    
    /**
     * Get the full address string from Geocoder.
     */
    private suspend fun getAddress(latitude: Double, longitude: Double): Address? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ uses async API
            suspendCancellableCoroutine { continuation ->
                try {
                    geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                        continuation.resume(addresses.firstOrNull())
                    }
                } catch (e: Exception) {
                    continuation.resume(null)
                }
            }
        } else {
            // Legacy API for older Android versions
            @Suppress("DEPRECATION")
            try {
                geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()
            } catch (e: Exception) {
                null
            }
        }
    }
    
    /**
     * Format an Address object into a readable string.
     * Prefers street name + locality for start/end locations.
     */
    private fun formatAddress(address: Address): String {
        val parts = mutableListOf<String>()
        
        // Add thoroughfare (street name) with sub-thoroughfare (street number)
        val street = when {
            address.thoroughfare != null && address.subThoroughfare != null -> 
                "${address.subThoroughfare} ${address.thoroughfare}"
            address.thoroughfare != null -> address.thoroughfare
            address.subThoroughfare != null -> address.subThoroughfare
            else -> null
        }
        
        street?.let { parts.add(it) }
        
        // Add locality (city/neighborhood)
        when {
            address.locality != null -> parts.add(address.locality)
            address.subLocality != null -> parts.add(address.subLocality)
            address.adminArea != null -> parts.add(address.adminArea)
        }
        
        // If we have both street and city, format nicely
        return if (parts.size >= 2) {
            "${parts[0]}, ${parts[1]}"
        } else if (parts.isNotEmpty()) {
            parts[0]
        } else {
            // Fallback to feature name or address lines
            address.featureName ?: address.getAddressLine(0) ?: "Unknown Location"
        }
    }
    
    /**
     * Get a short location name for display (just neighborhood or city).
     */
    suspend fun getShortLocationName(location: Location?): String = withContext(Dispatchers.IO) {
        if (location == null) return@withContext "Unknown"
        
        try {
            val address = getAddress(location.latitude, location.longitude)
            address?.let {
                it.subLocality ?: it.locality ?: it.thoroughfare ?: it.featureName
            } ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    private fun Double.format(decimals: Int) = "%.${decimals}f".format(this)
}
