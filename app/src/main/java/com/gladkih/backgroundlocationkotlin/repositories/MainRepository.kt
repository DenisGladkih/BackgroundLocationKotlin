package com.gladkih.backgroundlocationkotlin.repositories

import com.gladkih.backgroundlocationkotlin.db.UserLocation
import com.gladkih.backgroundlocationkotlin.db.UserLocationDAO
import javax.inject.Inject

class MainRepository @Inject constructor(
    private val userLocationDAO: UserLocationDAO
) {

    suspend fun insertUserLocation(userLocation: UserLocation) =
        userLocationDAO.insertUserLocation(userLocation)

    suspend fun deleteAllUserLocations() =
        userLocationDAO.deleteAllUserLocations()

    fun getAllUserLocations() = userLocationDAO.getAllUserLocations()

}