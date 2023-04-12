package com.example.mattatoyomng.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val userid: String = "",
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val profilePic: String = "",
    val admin: Boolean = false
) : Parcelable