package com.udacity.nanodegree.locationreminder.locationreminders.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udacity.nanodegree.locationreminder.R
import com.udacity.nanodegree.locationreminder.locationreminders.savereminder.SaveReminderFragment

/**
 * Triggered by the Geofence.  Since we can have many Geofences at once, we pull the request
 * ID from the first Geofence, and locate it within the cached data in our Room DB
 *
 * Or users can add the reminders and then close the app, So our app has to run in the background
 * and handle the geofencing in the background.
 * To do that you can use https://developer.android.com/reference/android/support/v4/app/JobIntentService to do that.
 *
 */

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    companion object {
        val TAG: String = GeofenceBroadcastReceiver::class.java.simpleName
    }

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action == SaveReminderFragment.ACTION_GEOFENCE_EVENT) {
            val geoFencingEvent = GeofencingEvent.fromIntent(intent)

            if (geoFencingEvent.hasError()) return
            if (geoFencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER && geoFencingEvent.triggeringGeofences.isNotEmpty())
                GeofenceTransitionsJobIntentService.enqueueWork(context, intent)
        }

    }
}