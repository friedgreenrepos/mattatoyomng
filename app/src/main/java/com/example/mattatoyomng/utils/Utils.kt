package com.example.mattatoyomng.utils

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

fun dateFormatter(date: Timestamp?): String{
    val dateFormat = Constants.DATE_FORMAT
    val sdfDate = SimpleDateFormat(dateFormat, Locale.getDefault())
    return sdfDate.format(date!!.toDate()).toString()
}

fun timeFormatter(date: Timestamp?): String{
    val timeFormat = Constants.TIME_FORMAT
    val sdfDate = SimpleDateFormat(timeFormat, Locale.getDefault())
    return sdfDate.format(date!!.toDate()).toString()
}

fun stripSpaces(s: String): String {
    return s.replace(" ", "")
}
