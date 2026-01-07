package com.jonaylor.saintjohn.domain.usecase

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

data class LocationData(
    val latitude: Double,
    val longitude: Double
)

@Singleton
class GetLocationUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun getCurrentLocation(forceHighAccuracy: Boolean = false): LocationData? {
        if (!hasLocationPermission()) {
            return null
        }

        return try {
            suspendCancellableCoroutine { continuation ->
                val cancellationTokenSource = CancellationTokenSource()

                // Use HIGH_ACCURACY when user explicitly refreshes for fresh location
                // Use BALANCED_POWER_ACCURACY for automatic updates to save battery
                val priority = if (forceHighAccuracy) {
                    Priority.PRIORITY_HIGH_ACCURACY
                } else {
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY
                }

                fusedLocationClient.getCurrentLocation(
                    priority,
                    cancellationTokenSource.token
                ).addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        continuation.resume(
                            LocationData(
                                latitude = location.latitude,
                                longitude = location.longitude
                            )
                        )
                    } else {
                        // If location request fails, try last known location
                        tryLastKnownLocation(continuation)
                    }
                }.addOnFailureListener {
                    // On failure, try to get last known location
                    tryLastKnownLocation(continuation)
                }

                continuation.invokeOnCancellation {
                    cancellationTokenSource.cancel()
                }
            }
        } catch (e: SecurityException) {
            null
        }
    }

    private fun tryLastKnownLocation(continuation: kotlin.coroutines.Continuation<LocationData?>) {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    // Only use last known location if it's less than 30 minutes old
                    val locationAge = System.currentTimeMillis() - location.time
                    val thirtyMinutesInMillis = 30 * 60 * 1000

                    if (locationAge < thirtyMinutesInMillis) {
                        continuation.resume(
                            LocationData(
                                latitude = location.latitude,
                                longitude = location.longitude
                            )
                        )
                    } else {
                        // Location is too old, don't use it
                        continuation.resume(null)
                    }
                } else {
                    continuation.resume(null)
                }
            }.addOnFailureListener {
                continuation.resume(null)
            }
        } catch (e: SecurityException) {
            continuation.resume(null)
        }
    }
}
