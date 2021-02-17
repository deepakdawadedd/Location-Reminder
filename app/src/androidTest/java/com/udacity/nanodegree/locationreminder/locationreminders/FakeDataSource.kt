package com.udacity.nanodegree.locationreminder.locationreminders

import com.udacity.nanodegree.locationreminder.locationreminders.data.ReminderDataSource
import com.udacity.nanodegree.locationreminder.locationreminders.data.dto.ReminderDTO
import com.udacity.nanodegree.locationreminder.locationreminders.data.dto.Result

class FakeDataSource(var reminders: MutableList<ReminderDTO>? = mutableListOf()) :
    ReminderDataSource {


    override suspend fun getReminders(): Result<List<ReminderDTO>> = reminders?.let {
        Result.Success(ArrayList(it))
    } ?: Result.Error("No reminder found")

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> =
        reminders?.firstOrNull { it.id == id }?.let {
            Result.Success(it)
        } ?: Result.Error("Reminder $id not found")

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }
}