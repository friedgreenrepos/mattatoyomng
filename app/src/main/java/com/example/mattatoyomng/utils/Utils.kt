package com.example.mattatoyomng.utils

import android.content.Context
import android.view.View
import com.google.firebase.Timestamp
import java.util.Calendar

fun dateFormatter(date: Timestamp?, context: Context): String {
    val formatter = android.text.format.DateFormat.getLongDateFormat(context)
    return formatter.format(date!!.toDate()).toString()
}

fun timeFormatter(date: Timestamp?, context: Context): String {
    val formatter = android.text.format.DateFormat.getTimeFormat(context)
    return formatter.format(date!!.toDate()).toString()
}

fun View.hide() {
    visibility = View.GONE
}

fun View.show() {
    visibility = View.VISIBLE
}

fun getTodayTimestamp(): Timestamp {
    // define today
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    // cast Date to Timestamp
    return Timestamp(cal.time)
}

fun getNeverTimestamp(): Timestamp {
    // define today
    val cal = Calendar.getInstance()
    cal.set(Calendar.YEAR, 2100)
    // cast Date to Timestamp
    return Timestamp(cal.time)
}

fun stripSpaces(s: String): String {
    return s.replace(" ", "")
}