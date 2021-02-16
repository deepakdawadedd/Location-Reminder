package com.udacity.nanodegree.locationreminder.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.nanodegree.locationreminder.locationreminders.MainAndroidTestCoroutineRule
import com.udacity.nanodegree.locationreminder.locationreminders.data.dto.ReminderDTO
import com.udacity.nanodegree.locationreminder.locationreminders.data.dto.Result.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class RemindersLocalRepositoryTest {

    @get:Rule
    var mainCoroutineRule = MainAndroidTestCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    private lateinit var database: RemindersDatabase
    private lateinit var remindersDAO: RemindersDao
    private lateinit var repository: RemindersLocalRepository

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        remindersDAO = database.reminderDao()
        repository =
            RemindersLocalRepository(
                remindersDAO,
                Dispatchers.Main
            )
    }

    @After
    fun closeDB() {
        database.close()
    }

    @Test
    fun saveReminderAndGetByID() = mainCoroutineRule.runBlockingTest {
        val reminder = ReminderDTO(
            title = "Basketball",
            description = "Don't get crossed up or dunked on!",
            location = "B-Ball Court",
            latitude = 75.1234,
            longitude = 3333.1234
        )
        repository.saveReminder(reminder)
        val reminderLoaded = repository.getReminder(reminder.id) as Success<ReminderDTO>
        val loaded = reminderLoaded.data

        MatcherAssert.assertThat(loaded, Matchers.notNullValue())
        MatcherAssert.assertThat(loaded.id, CoreMatchers.`is`(reminder.id))
        MatcherAssert.assertThat(loaded.description, CoreMatchers.`is`(reminder.description))
        MatcherAssert.assertThat(loaded.location, CoreMatchers.`is`(reminder.location))
        MatcherAssert.assertThat(loaded.latitude, CoreMatchers.`is`(reminder.latitude))
        MatcherAssert.assertThat(loaded.longitude, CoreMatchers.`is`(reminder.longitude))
    }

    @Test
    fun deleteAllRemindersAndReminders() = mainCoroutineRule.runBlockingTest {
        val reminder = ReminderDTO(
            title = "Basketball",
            description = "Don't get crossed up or dunked on!",
            location = "B-Ball Court",
            latitude = 75.1234,
            longitude = 3333.1234
        )
        repository.saveReminder(reminder)
        repository.deleteAllReminders()
        val reminders =
            repository.getReminders() as Success<List<ReminderDTO>>
        val data = reminders.data
        MatcherAssert.assertThat(data.isEmpty(), CoreMatchers.`is`(true))

    }

    @Test
    fun noRemindersFoundGetReminderById() = mainCoroutineRule.runBlockingTest {
        val reminder = repository.getReminder("3") as Error
        MatcherAssert.assertThat(reminder.message, Matchers.notNullValue())
        MatcherAssert.assertThat(reminder.message, CoreMatchers.`is`("Reminder not found!"))
    }
}