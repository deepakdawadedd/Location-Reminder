package com.udacity.nanodegree.locationreminder.locationreminders.savereminder

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.nanodegree.locationreminder.R
import com.udacity.nanodegree.locationreminder.base.BaseViewModel
import com.udacity.nanodegree.locationreminder.base.NavigationCommand
import com.udacity.nanodegree.locationreminder.locationreminders.data.ReminderDataSource
import com.udacity.nanodegree.locationreminder.locationreminders.data.dto.ReminderDTO
import com.udacity.nanodegree.locationreminder.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.launch

class SaveReminderViewModel(val app: Application, val dataSource: ReminderDataSource) :
    BaseViewModel(app) {
    val reminderTitle = MutableLiveData<String>()
    val reminderDescription = MutableLiveData<String>()
    val reminderSelectedLocationStr = MutableLiveData<String>()
    val selectedPOI = MutableLiveData<PointOfInterest>()
    val latitude = MutableLiveData<Double>()
    val longitude = MutableLiveData<Double>()

    /**
     * Clear the live data objects to start fresh next time the view model gets called
     */
    fun onClear() {
        reminderTitle.value = null
        reminderDescription.value = null
        reminderSelectedLocationStr.value = null
        selectedPOI.value = null
        latitude.value = null
        longitude.value = null
    }

    /**
     * Validate the entered data then saves the reminder data to the DataSource
     */
    fun validateAndSaveReminder(reminderData: ReminderDataItem): ReminderDTO? {
        return if (validateEnteredData(reminderData))
            saveReminder(reminderData)
        else null
    }

    /**
     * Save the reminder to the data source
     */
    private fun saveReminder(reminderData: ReminderDataItem): ReminderDTO {
        showLoading.value = true
        val reminder = ReminderDTO(
            reminderData.title,
            reminderData.description,
            reminderData.location,
            reminderData.latitude,
            reminderData.longitude,
            reminderData.id
        )

        viewModelScope.launch {
            dataSource.saveReminder(reminder)
            showLoading.value = false
            showToast.value = app.getString(R.string.reminder_saved)
            navigationCommand.value = NavigationCommand.Back
        }
        return reminder
    }

    /**
     * Validate the entered data and show error to the user if there's any invalid data
     */
    private fun validateEnteredData(reminderData: ReminderDataItem): Boolean {
        if (reminderData.title.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_enter_title
            return false
        }

        if (reminderData.location.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_select_location
            return false
        }
        return true
    }
}