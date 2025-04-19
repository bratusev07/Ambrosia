package ru.bratusev.ambrosia.ui

import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient

class LocationUtils(
    private val context: Context,
    private val locationClient: FusedLocationProviderClient
) {
    fun getLastLocation(
        onSuccess: (Location) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            locationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        onSuccess(location)
                    } else {
                        onFailure(Exception("Местоположение недоступно"))
                    }
                }
                .addOnFailureListener { e ->
                    onFailure(e)
                }
        } catch (e: SecurityException) {
            onFailure(e)
        }
    }
}