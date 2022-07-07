package com.gladkih.backgroundlocationkotlin.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng

@Entity(tableName = "user_location_table")
data class UserLocation(
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null

    fun getLatLng() = LatLng(latitude, longitude)
}
