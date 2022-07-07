package com.gladkih.backgroundlocationkotlin.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [UserLocation::class],
    version = 1,
    exportSchema = false
)
abstract class UserLocationDatabase:RoomDatabase () {
    abstract fun getUserLocationDao():UserLocationDAO
}