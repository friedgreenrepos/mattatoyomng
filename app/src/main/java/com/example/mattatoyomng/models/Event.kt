package com.example.mattatoyomng.models

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Event(
    val title: String = "",
    val description: String = "",
    val owner: String = "",
    val date: Timestamp? = null,
    val eventImgURL: String = "",
    val tags: MutableList<String> = arrayListOf(),
    val keywords: String = "",
    val userReminderMap: MutableMap<String, Timestamp> = mutableMapOf(),
    var documentId: String = "",
) : Parcelable