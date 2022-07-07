package com.gladkih.backgroundlocationkotlin.di

import android.content.Context
import androidx.room.Room
import com.gladkih.backgroundlocationkotlin.db.UserLocationDatabase
import com.gladkih.backgroundlocationkotlin.other.Constants.USER_LOCATION_DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideUserLocationDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        UserLocationDatabase::class.java,
        USER_LOCATION_DATABASE_NAME
    ).build()


    @Singleton
    @Provides
    fun provideUserLocationDao(db: UserLocationDatabase) = db.getUserLocationDao()

}