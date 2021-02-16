package com.udacity.nanodegree.locationreminder.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.nanodegree.locationreminder.data.FakeDataSource
import com.udacity.nanodegree.locationreminder.locationreminders.data.dto.ReminderDTO
import com.udacity.nanodegree.locationreminder.locationreminders.savereminder.SaveReminderViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val reminder1 = ReminderDTO("Title1", "Description1", "location1", 0.53, 0.6, "0")
    private val reminder2 = ReminderDTO("Title2", "Description2", "location2", 0.53, 0.6, "1")
    private val reminder3 = ReminderDTO("Title3", "Description3", "location3", 0.53, 0.6, "2")
    private val remoteReminders = listOf(reminder1, reminder2, reminder3).sortedBy { it.id }

    private lateinit var reminderDataSource: FakeDataSource
    private lateinit var saveReminderViewModel: SaveReminderViewModel


    @Before
    fun createViewModel() {
        reminderDataSource = FakeDataSource(remoteReminders.toMutableList())
        saveReminderViewModel =
            SaveReminderViewModel(ApplicationProvider.getApplicationContext(), reminderDataSource)

    }

    @Test
    fun onClear() {
        saveReminderViewModel.onClear()
    }

}