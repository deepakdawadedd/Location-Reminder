package com.udacity.nanodegree.locationreminder.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.nanodegree.locationreminder.MainCoroutineRule
import com.udacity.nanodegree.locationreminder.data.FakeDataSource
import com.udacity.nanodegree.locationreminder.getOrAwaitValue
import com.udacity.nanodegree.locationreminder.locationreminders.reminderslist.RemindersListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.P])
class RemindersListViewModelTest {

    @get: Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get: Rule
    var mainCoroutineRule = MainCoroutineRule()
    private lateinit var remindersListViewModel: RemindersListViewModel

    @Before
    fun setUp() {
        stopKoin()

        remindersListViewModel = RemindersListViewModel(
                ApplicationProvider.getApplicationContext(),
                FakeDataSource()
            )

    }

    @Test
    fun check_loading_status() = mainCoroutineRule.runBlockingTest {
        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()
        var loading = remindersListViewModel.showLoading.getOrAwaitValue()
        assertThat(
            loading,
            `is`(true)
        )
        mainCoroutineRule.resumeDispatcher()
        loading = remindersListViewModel.showLoading.getOrAwaitValue()
        assertThat(
            loading,
            `is`(false)
        )
    }
}