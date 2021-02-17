package com.udacity.nanodegree.locationreminder.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.nanodegree.locationreminder.MainCoroutineRule
import com.udacity.nanodegree.locationreminder.R
import com.udacity.nanodegree.locationreminder.data.FakeDataSource
import com.udacity.nanodegree.locationreminder.getOrAwaitValue
import com.udacity.nanodegree.locationreminder.locationreminders.reminderslist.ReminderDataItem
import com.udacity.nanodegree.locationreminder.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class SaveReminderViewModelTest {

    @get: Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get: Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var saveReminderViewModel: SaveReminderViewModel

    private lateinit var fakeDataSource: FakeDataSource

    @Before
    fun init() {
        stopKoin()
        fakeDataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeDataSource
        )

    }


    @Test
    fun check_loading_status() = mainCoroutineRule.runBlockingTest {
        mainCoroutineRule.pauseDispatcher()
        val reminderDataItem = ReminderDataItem(
            "Testing title of reminder",
            "This is a demo Description for testing",
            "Test Location",
            0.0,
            0.0
        )
        saveReminderViewModel.saveReminder(reminderDataItem)
        var loadingStatus = saveReminderViewModel.showLoading.getOrAwaitValue()
        assertThat(
            loadingStatus,
            `is`(true)
        )

        mainCoroutineRule.resumeDispatcher()
        loadingStatus = saveReminderViewModel.showLoading.getOrAwaitValue()
        assertThat(
            loadingStatus,
            `is`(false)
        )
    }

    @Test
    fun returnError() = mainCoroutineRule.runBlockingTest {
        val reminderDataItem = ReminderDataItem(
            "Testing title of reminder",
            "This is a  Description for testing",
            "Test",
            0.0,
            0.0
        )
        val valid = saveReminderViewModel.validateEnteredData(reminderDataItem)
        assertThat(valid, `is`(false))
        val snackBarMessage = saveReminderViewModel.showSnackBarInt.getOrAwaitValue()
        assertThat(
            snackBarMessage,
            `is`(R.string.err_enter_title)
        )
    }
}