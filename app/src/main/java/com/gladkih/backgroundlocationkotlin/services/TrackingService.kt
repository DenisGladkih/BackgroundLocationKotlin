package com.gladkih.backgroundlocationkotlin.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.gladkih.backgroundlocationkotlin.R
import com.gladkih.backgroundlocationkotlin.db.UserLocation
import com.gladkih.backgroundlocationkotlin.other.Constants.ACTION_START_SERVICE
import com.gladkih.backgroundlocationkotlin.other.Constants.ACTION_STOP_SERVICE
import com.gladkih.backgroundlocationkotlin.other.Constants.LOCATION_UPDATE_INTERVAL
import com.gladkih.backgroundlocationkotlin.other.Constants.NOTIFICATION_CHANNEL_ID
import com.gladkih.backgroundlocationkotlin.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.gladkih.backgroundlocationkotlin.other.Constants.NOTIFICATION_ID
import com.gladkih.backgroundlocationkotlin.other.Constants.SERVICE_TAG
import com.gladkih.backgroundlocationkotlin.other.KalmanFilter
import com.gladkih.backgroundlocationkotlin.repositories.MainRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


@AndroidEntryPoint
class TrackingService : LifecycleService() {

    @Inject
    lateinit var repository: MainRepository

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var kalmanFilter: KalmanFilter


    companion object {
        val isTracking = MutableLiveData<Boolean>()
    }

    override fun onCreate() {
        super.onCreate()

        kalmanFilter = KalmanFilter()
        fusedLocationClient = FusedLocationProviderClient(this)


        isTracking.observe(this) {
            updateLocation(it)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent.let {
            when (it?.action) {
                ACTION_START_SERVICE -> startForegroundService()
                ACTION_STOP_SERVICE -> stopForegroundService()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForegroundService() {
        Log.d(SERVICE_TAG, "Start service")
        isTracking.postValue(true)
        deleteAllUserLocationsFromDatabase()


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(getString(R.string.location_service_title))
            .setContentText(getString(R.string.location_service_text))

        startForeground(NOTIFICATION_ID, notification.build())
    }

    private fun stopForegroundService() {
        Log.d(SERVICE_TAG, "Stop service")
        isTracking.postValue(false)
        stopForeground(true)
        stopSelf()
    }


    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {

            locationResult.locations.let { locations ->
                for (location in locations) {
                    kalmanFilter.process(location)
                }

                kalmanFilter.getNewLatLng().let {
                    saveUserLocationToDatabase(UserLocation(it.latitude, it.longitude))
                }
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun updateLocation(isTracking: Boolean) {

        if (isTracking) {

            val request = LocationRequest.create().apply {
                interval = LOCATION_UPDATE_INTERVAL
                priority = PRIORITY_HIGH_ACCURACY
            }

            fusedLocationClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )

        } else {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }


    private fun saveUserLocationToDatabase(userLocation: UserLocation) = runBlocking {
        launch {
            repository.insertUserLocation(userLocation)
        }
    }


    private fun deleteAllUserLocationsFromDatabase() = runBlocking {
        launch {
            repository.deleteAllUserLocations()
        }
    }
}
