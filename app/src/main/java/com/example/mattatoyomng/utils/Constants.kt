package com.example.mattatoyomng.utils

object Constants {
    // Firebase Constants:
    // 1. collection for users
    // 2. collection for events
    const val USERS: String = "users"
    const val EVENTS: String = "events"

    // Firestore db field names
    // User class
    const val USERID: String = "userid"
    const val NAME: String = "name"
    const val USERNAME: String = "username"
    const val EMAIL: String = "email"
    const val PROFILE_PIC: String = "profilePic"
    // Event class
    const val TITLE: String = "title"
    const val DESCRIPTION: String = "description"
    const val DATE: String = "date"
    const val EVENT_IMAGE_URL: String = "eventImgURL"
    const val TAGS: String = "tags"
    const val REMINDER: String = "userReminderMap"
    const val KEYWORDS: String = "keywords"
}