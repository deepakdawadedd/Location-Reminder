package com.udacity.nanodegree.locationreminder.locationreminders.reminderslist

import com.udacity.nanodegree.locationreminder.R
import com.udacity.nanodegree.locationreminder.base.BaseRecyclerViewAdapter
import com.udacity.nanodegree.locationreminder.locationreminders.reminderslist.ReminderDataItem


//Use data binding to show the reminder on the item
class RemindersListAdapter(callBack: (selectedReminder: ReminderDataItem) -> Unit) :
    BaseRecyclerViewAdapter<ReminderDataItem>(callBack) {
    override fun getLayoutRes(viewType: Int) = R.layout.it_reminder
}