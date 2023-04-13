package com.example.mattatoyomng.utils

import android.content.Context
import com.google.firebase.Timestamp
import java.util.*

fun dateFormatter(date: Timestamp?, context: Context): String{
    val formatter = android.text.format.DateFormat.getLongDateFormat(context)
    return formatter.format(date!!.toDate()).toString()
}

fun timeFormatter(date: Timestamp?, context: Context): String{
    val formatter = android.text.format.DateFormat.getTimeFormat(context)
    return formatter.format(date!!.toDate()).toString()
}

fun stripSpaces(s: String): String {
    return s.replace(" ", "")
}