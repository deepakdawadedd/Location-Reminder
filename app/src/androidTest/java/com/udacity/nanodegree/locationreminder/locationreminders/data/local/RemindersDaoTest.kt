package com.udacity.nanodegree.locationreminder.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.nanodegree.locationreminder.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class RemindersDaoTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    /**
     * initializes database so the information stored here will clear when the process was killed.
     */
    @Before
    fun initialiseDataBase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    /**
     * this will close the database if open
     * */
    @After
    fun closeDataBase() = database.close()

    @Test
    fun saveReminder_getReminderById_existInDB() = runBlockingTest {
        val reminder = ReminderDTO(
            title = "FootBall",
            description = "Upcoming football match at town",
            location = "chinna swammy stadium",
            latitude = 1099.19,
            longitude = 1299.14
        )
        database.reminderDao().saveReminder(reminder)

        val dto = database.reminderDao().getReminderById(reminder.id)
        dto?.apply {
            assertThat(this, notNullValue())
            assertThat(id, `is`(reminder.id))
            assertThat(description, `is`(reminder.description))
            assertThat(location, `is`(reminder.location))
            assertThat(latitude, `is`(reminder.latitude))
            assertThat(longitude, `is`(reminder.longitude))
        }
    }

    @Test
    fun deleteAllReminders_remindersEmpty() = runBlockingTest {
        val reminder = ReminderDTO(
            title = "FootBall",
            description = "Upcoming football match at town",
            location = "chinna swammy stadium",
            latitude = 1099.19,
            longitude = 1299.14
        )

        database.reminderDao().saveReminder(reminder)
        database.reminderDao().deleteAllReminders()
        val reminders = database.reminderDao().getReminders()
        assertThat(reminders.isEmpty(), `is`(true))

    }

    @Test
    fun getReminder_byId_reminderFound_zero() = runBlockingTest {
        val reminder = database.reminderDao().getReminderById("some random id")
        assertThat(reminder, nullValue())

    }
}