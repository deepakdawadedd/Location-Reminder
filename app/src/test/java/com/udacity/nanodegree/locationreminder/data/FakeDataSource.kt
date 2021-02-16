package com.udacity.nanodegree.locationreminder.data

import com.udacity.nanodegree.locationreminder.locationreminders.data.ReminderDataSource
import com.udacity.nanodegree.locationreminder.locationreminders.data.dto.ReminderDTO
import com.udacity.nanodegree.locationreminder.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminders: MutableList<ReminderDTO> = mutableListOf() ) : ReminderDataSource {

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        reminders.let { return Result.Success(ArrayList(it)) }


    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        return Result.Success(reminders[id.toInt()])
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }


}