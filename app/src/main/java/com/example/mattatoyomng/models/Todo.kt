package com.example.mattatoyomng.models

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Todo(
    var text: String = "",
    var dateAdded: Timestamp? = null,
    var done: Boolean = false,
    var documentId: String = "",
) : Parcelable
