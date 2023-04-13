package com.example.mattatoyomng.utils

object Constants {
    // Firebase Constants:
    // 1. collection for users
    // 2. collection for events
    const val USERS: String = "users"
    const val EVENTS: String = "events"

    // Firestore db field names
    const val PROFILE_PIC: String = "profilePic"
    const val NAME: String = "name"
    const val USERNAME: String = "username"
    const val TITLE: String = "title"
    const val DESCRIPTION: String = "description"
    const val DATE: String = "date"
    const val EVENT_IMAGE_URL: String = "eventImgURL"
    const val TAGS: String = "tags"
    const val REMINDER: String = "reminderTimestamp"
    const val KEYWORDS: String = "keywords"
    // date formatting
    const val DATE_FORMAT: String = "dd MMMM yyyy"
    const val TIME_FORMAT: String = "hh:mm"
}