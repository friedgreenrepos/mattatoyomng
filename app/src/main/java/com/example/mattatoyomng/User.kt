package com.example.mattatoyomng

import android.app.Application

//data class User(
//    val name: String,
//    val surname: String,
//    val username: String,
//    val profilePic: Int,
//    val admin: Boolean)
class User : Application(){
    val name: String? = null
    val surname: String? = null
    val email: String? = null
    val userid: String? = null
    val admin: Boolean = false

    companion object {
        var instance: User? = null
        get() {
            if (field == null){
                // create new instance
                field = User()
            }
            return field
        }
        private set
    }
}