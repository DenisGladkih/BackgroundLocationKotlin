package com.gladkih.backgroundlocationkotlin.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface UserLocationDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserLocation(userLocation: UserLocation)

    @Query("SELECT * FROM user_location_table")
    fun getAllUserLocations(): LiveData<List<UserLocation>>

    @Query("DELETE FROM user_location_table")
    suspend fun deleteAllUserLocations()

}