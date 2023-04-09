package com.example.mattatoyomng.firebase

import android.app.Activity
import android.util.Log
import com.example.mattatoyomng.activities.LoginActivity
import com.example.mattatoyomng.activities.MainActivity
import com.example.mattatoyomng.activities.RegisterActivity
import com.example.mattatoyomng.models.User
import com.example.mattatoyomng.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirestoreClass {

    private val dbFirestore = FirebaseFirestore.getInstance()

    /**
     * A function to make an entry of the registered user in the firestore database.
     */
    fun registerUser(activity: RegisterActivity, userInfo: User) {

        dbFirestore.collection(Constants.USERS)
            // Get document for current user
            .document(getCurrentUserID())
            // merge User info with user document
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {

                // Here call a function of base activity for transferring the result to it.
                activity.userRegisteredSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(
                    activity.javaClass.simpleName,
                    "Error writing document",
                    e
                )
            }
    }

    fun loginUser(activity: LoginActivity) {
        dbFirestore.collection(Constants.USERS)
            // Get document for current user
            .document(getCurrentUserID())
            // merge User info with user document
            .get()
            .addOnSuccessListener { document ->
                val loggedInUser = document.toObject(User::class.java)!!
                activity.userLoginSuccess(loggedInUser)
            }
            .addOnFailureListener { e ->
                Log.e(
                    activity.javaClass.simpleName,
                    "Error writing document",
                    e
                )
            }
    }

    fun loadUserData(activity: Activity) {

        // Here we pass the collection name from which we wants the data.
        dbFirestore.collection(Constants.USERS)
            // The document id to get the Fields of user.
            .document(getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.toString())

                // Here we have received the document snapshot which is converted into the User Data model object.
                val loggedInUser = document.toObject(User::class.java)!!

                // Here call a function of base activity for transferring the result to it.
                when (activity) {
                    is LoginActivity -> {
                        activity.userLoginSuccess(loggedInUser)
                    }
                    is MainActivity -> {
                        activity.updateNavigationUserDetails(loggedInUser)
                    }
                    // TODO: implement User Profile Activity
//                    is MyProfileActivity -> {
//                        activity.setUserDataInUI(loggedInUser)
//                    }
                }
            }
            .addOnFailureListener { e ->
                // Here call a function of base activity for transferring the result to it.
                when (activity) {
                    is LoginActivity -> {
                        activity.showErrorSnackBar("Error loading user data")
                    }
                    is MainActivity -> {
                        activity.showErrorSnackBar("Error loading user data")
                    }
                    // TODO: implement User Profile Activity
//                    is MyProfileActivity -> {
//                        activity.showErrorSnackBar("Error loading user data")
//                    }
                }
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while getting loggedIn user details",
                    e
                )
            }
    }

    // Function to get the userid of current logged user. Return empty string if no user is logged in.
    fun getCurrentUserID(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID = ""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }
        return currentUserID
    }
}