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
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.*
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

    @get:Rule var mainCoroutineRule = MainAndroidTestCoroutineRule()

    @get:Rule var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    private lateinit var remindersDAO: RemindersDao

    private lateinit var repository: RemindersLocalRepository

    /**
     * initializes database so the information stored here will clear when the process was killed.
     */
    @Before
    fun initialSetup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        remindersDAO = database.reminderDao()
        repository =
            RemindersLocalRepository(
                remindersDAO,
                Dispatchers.Main
            )
    }

    /**
     * this will close the database if open
     * */
    @After
    fun closeDataBase() = database.close()

    @Test //WHEN: saving data in db, THEN: retrieving saved data, RESULT: retrieved data matches with saved data
    fun saveReminder_getReminderById_existInDB() = mainCoroutineRule.runBlockingTest {
        val reminder = ReminderDTO(
            title = "FootBall",
            description = "Upcoming football match at town",
            location = "chinna swammy stadium",
            latitude = 1099.19,
            longitude = 1299.14
        )
        repository.saveReminder(reminder)
        val reminderLoaded = repository.getReminder(reminder.id) as Success<ReminderDTO>
        val reminderDTO = reminderLoaded.data
        reminderDTO.apply {
            assertThat(this, Matchers.notNullValue())
            assertThat(id, `is`(reminder.id))
            assertThat(description, `is`(reminder.description))
            assertThat(location, `is`(reminder.location))
            assertThat(latitude, `is`(reminder.latitude))
            assertThat(longitude, `is`(reminder.longitude))
        }
    }

    @Test //WHEN: deleting all data from database, THEN: getting data, RESULT: showing data is Empty
    fun deleteAllReminders_getReminder_dataIsEmpty() = mainCoroutineRule.runBlockingTest {
        val reminder = ReminderDTO(
            title = "FootBall",
            description = "Upcoming football match at town",
            location = "chinna swammy stadium",
            latitude = 1099.19,
            longitude = 1299.14
        )
        repository.saveReminder(reminder)
        repository.deleteAllReminders()
        val reminders= repository.getReminders() as Success<List<ReminderDTO>>
        val isEmpty = reminders.data.isEmpty()
        assertThat(isEmpty, `is`(true))

    }

    @Test
    fun getReminderById_noRemindersFound_message() = mainCoroutineRule.runBlockingTest {
        val reminder = repository.getReminder("random id") as Error
        val message = reminder.message
        assertThat(message, Matchers.notNullValue())
        assertThat(message, `is`("Reminder not found!"))
    }
}
