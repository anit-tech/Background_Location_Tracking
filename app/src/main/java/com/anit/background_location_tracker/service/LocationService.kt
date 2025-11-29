package com.anit.background_location_tracker.service

import android.Manifest
import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.anit.background_location_tracker.CHANNEL_ID
import com.anit.background_location_tracker.R
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationService : Service() {

    private val locationRequest by lazy {
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).setIntervalMillis(
            10000
        ).build()
    }

    private val locationCallback by lazy {
        object : LocationCallback() {
            override fun onLocationAvailability(p0: LocationAvailability) {
                super.onLocationAvailability(p0)
            }

            override fun onLocationResult(location: LocationResult) {
                val lat = location.lastLocation?.latitude.toString()
                val long = location.lastLocation?.longitude.toString()
                val speed = location.lastLocation?.speed.toString()
                val accuracy = location.lastLocation?.accuracy.toString()
                val timeStamp = location.lastLocation?.time.toString()
                startServiceOfForeground(lat,long,speed,accuracy,timeStamp)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        locationUpdates()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun locationUpdates() {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,locationCallback,null
        )
    }

    private fun startServiceOfForeground(lat: String,long: String,speed: String,accuracy: String,time: String) {
        val gmmIntentUri = "geo:$lat,$long?q=$lat,$long(Current Location)".toUri()
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
            setPackage("com.google.android.apps.maps")
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            mapIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("Anit Location Update")
            .setContentIntent(pendingIntent)
            .setContentText("Latitude: $lat --- Longitude: $long --- Speed: $speed --- Accuracy: $accuracy --- Timestamp:$time")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startForeground(1,notification)
            }else{
                startForeground(1,notification)
            }
        }
    }
}