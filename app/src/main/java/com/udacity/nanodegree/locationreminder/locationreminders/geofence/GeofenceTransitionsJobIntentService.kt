package com.udacity.nanodegree.locationreminder.locationreminders.geofence

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udacity.nanodegree.locationreminder.locationreminders.data.ReminderDataSource
import com.udacity.nanodegree.locationreminder.locationreminders.data.dto.ReminderDTO
import com.udacity.nanodegree.locationreminder.locationreminders.data.dto.Result
import com.udacity.nanodegree.locationreminder.locationreminders.reminderslist.ReminderDataItem
import com.udacity.nanodegree.locationreminder.utils.sendNotification
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope {

    private val repository: ReminderDataSource by inject()
    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob


    companion object {
        private val TAG: String = GeofenceTransitionsJobIntentService::class.java.simpleName
        private const val JOB_ID = 573

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                context,
                GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                intent
            )
        }
    }

    override fun onHandleWork(intent: Intent) {
        val geoFencingEvent = GeofencingEvent.fromIntent(intent)
        if (geoFencingEvent.hasError()) return
        if (geoFencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geoFencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL)
            sendNotification(geoFencingEvent.triggeringGeofences)

    }

    private fun sendNotification(triggeringGeofences: List<Geofence>) {
        val requestId =
            if (triggeringGeofences.isNotEmpty()) triggeringGeofences[0].requestId else return

        if (requestId.isNullOrEmpty()) return
        //Get the local repository instance
        //Interaction to the repository has to be through a coroutine scope
        CoroutineScope(coroutineContext).launch(SupervisorJob()) {
            //get the reminder with the request id
            val result = repository.getReminder(requestId)
            if (result is Result.Success<ReminderDTO>) {
                val reminderDTO = result.data
                //send a notification to the user with the reminder details
                sendNotification(
                    this@GeofenceTransitionsJobIntentService, ReminderDataItem(
                        reminderDTO.title,
                        reminderDTO.description,
                        reminderDTO.location,
                        reminderDTO.latitude,
                        reminderDTO.longitude,
                        reminderDTO.id
                    )
                )
            }
        }
    }

}