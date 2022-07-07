package com.gladkih.backgroundlocationkotlin.other

import android.location.Location
import com.google.android.gms.maps.model.LatLng


class KalmanFilter {
    private var timeStamp: Long = 0
    private var latitude = 0.0
    private var longitude = 0.0
    private var variance = -1f


    fun process(location: Location) {
        if (variance < 0) {
            // if variance < 0, object is uninitialised, so initialise with current values
            latitude = location.latitude
            longitude = location.longitude
            timeStamp = location.time
            variance = location.accuracy * location.accuracy

        } else {
            // else apply Kalman filter methodology
            val duration = location.time - timeStamp
            if (duration > 0) {
                variance += duration * location.speed * location.speed / 1000
                timeStamp = location.time
            }

            val k = variance / (variance + location.accuracy * location.accuracy)
            latitude += k * (location.latitude - latitude)
            longitude += k * (location.longitude - longitude)
            variance *= (1 - k)
        }
    }

    fun getNewLatLng(): LatLng {
        return LatLng(latitude, longitude)
    }
}