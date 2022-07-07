package com.gladkih.backgroundlocationkotlin.other

import android.graphics.Color

object Constants {
    const val USER_LOCATION_DATABASE_NAME = "user_location_db"

    const val RC_LOCATION_PERMISSIONS = 1
    const val RC_BACKGROUND_LOCATION_PERMISSIONS = 2

    const val ACTION_START_SERVICE = "ACTION_START_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"

    const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
    const val NOTIFICATION_CHANNEL_NAME = "tracking"
    const val NOTIFICATION_ID = 1

    const val LOCATION_UPDATE_INTERVAL = 1000L

    const val POLYLINE_COLOR = Color.BLUE
    const val POLYLINE_WIDTH = 8f
    const val MAP_ZOOM = 16f

    const val SERVICE_TAG = "SERVICE_TAG"

}