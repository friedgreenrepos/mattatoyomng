package com.example.mattatoyomng.utils

object Constants {

    // Firebase Constants:
    // collection for users
    const val USERS: String = "users"
    // collection for events
    const val EVENTS: String = "events"
    // collection for todos
    const val TODOS: String = "todos"
    // storage folder name for user profile pictures
    const val USER_PICS_FOLDER: String = "user_profile_pics"
    // storage folder name for event images
    const val EVENT_IMAGES: String = "events_images"


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