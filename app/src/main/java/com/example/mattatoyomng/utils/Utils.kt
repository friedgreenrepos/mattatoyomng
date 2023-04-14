package com.example.mattatoyomng.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import com.example.mattatoyomng.R
import com.example.mattatoyomng.models.*
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

fun View.hide(){
    visibility = View.GONE
}

fun View.show(){
    visibility = View.VISIBLE
}

fun stripSpaces(s: String): String {
    return s.replace(" ", "")
}