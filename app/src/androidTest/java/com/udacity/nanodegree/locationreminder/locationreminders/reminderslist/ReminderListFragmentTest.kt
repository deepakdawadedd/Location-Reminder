package com.udacity.nanodegree.locationreminder.locationreminders.reminderslist

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.nanodegree.locationreminder.R
import com.udacity.nanodegree.locationreminder.locationreminders.FakeDataSource
import com.udacity.nanodegree.locationreminder.locationreminders.data.ReminderDataSource
import com.udacity.nanodegree.locationreminder.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {

    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var reminderListViewModel: RemindersListViewModel

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */

    @Before
    fun init() {
        fakeDataSource = FakeDataSource()
        reminderListViewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeDataSource as ReminderDataSource
        )

        stopKoin()

        val myModule = module {
            single {
                reminderListViewModel
            }
        }
        startKoin {
            modules(listOf(myModule))
        }
    }

    @Test
    fun remindersList_DisplayedInUi() {
        val reminder = ReminderDTO(
            "Reminder Testing",
            "This is a description for Testing",
            "Testing",
            0.0,
            0.0
        )
        runBlocking {
            fakeDataSource.saveReminder(reminder)
        }
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        onView(withText(reminder.title)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(withText(reminder.description)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(withText(reminder.location)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun reminders_navigateToAddReminder() {
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.requireView(), navController)
        }

        onView(
            withId(R.id.reminders_fragment_add_reminder_fab)
        ).perform(click())

        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }
    @Test
    fun remindersList_noReminder_ErrorSnackBarShown() = runBlockingTest {
        fakeDataSource.deleteAllReminders()
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        onView(withText("No Data"))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

}