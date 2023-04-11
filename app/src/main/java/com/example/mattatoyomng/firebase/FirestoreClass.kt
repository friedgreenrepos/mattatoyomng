package com.example.mattatoyomng.firebase

import android.app.Activity
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import com.example.mattatoyomng.R
import com.example.mattatoyomng.activities.CreateEventActivity
import com.example.mattatoyomng.activities.LoginActivity
import com.example.mattatoyomng.activities.MainActivity
import com.example.mattatoyomng.activities.RegisterActivity
import com.example.mattatoyomng.fragments.UpdateProfileFragment
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

    fun loadUserData(activity: Activity? = null, fragment: Fragment? = null) {
        // Here we pass the collection name from which we wants the data.
        dbFirestore.collection(Constants.USERS)
            // The document id to get the Fields of user.
            .document(getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                // Convert document to User object.
                val loggedInUser = document.toObject(User::class.java)!!

                if (activity != null) {
                    Log.i(activity.javaClass.simpleName, document.toString())
                    // Here call a function of base activity for transferring the result to it.
                    when (activity) {
                        is LoginActivity -> {
                            activity.userLoginSuccess(loggedInUser)
                        }
                        is MainActivity -> {
                            activity.updateNavigationUserDetails(loggedInUser)
                        }
                        is CreateEventActivity -> {
                            activity.setEventOwner(loggedInUser)
                        }
                    }
                }
                if (fragment != null) {
                    Log.i(fragment.javaClass.simpleName, document.toString())
                    when (fragment) {
                        is UpdateProfileFragment -> {
                            fragment.setUserDataInUI(loggedInUser)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                // Here call a function of base activity for transferring the result to it.
                if (activity != null) {
                    Log.e(
                        activity.javaClass.simpleName,
                        "Error while getting loggedIn user details",
                        e
                    )
                    when (activity) {
                        is LoginActivity -> {
                            activity.showErrorSnackBar("Error loading user data")
                        }
                        is MainActivity -> {
                            activity.showErrorSnackBar("Error loading user data")
                        }
                        is CreateEventActivity -> {
                            activity.showErrorSnackBar("Error loading user data")
                        }
                    }
                }
                if (fragment != null) {
                    Log.e(
                        fragment.javaClass.simpleName,
                        "Error while getting loggedIn user details",
                        e
                    )
                    when (fragment) {
                        is UpdateProfileFragment -> {
                            fragment.showErrorSnackBar("Error loading user data")
                        }
                    }
                }
            }
    }

    // Function to update user profile using hashmap
    fun updateUserProfileData(fragment: UpdateProfileFragment, userHashMap: HashMap<String, Any>) {
        dbFirestore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .update(userHashMap)
            .addOnSuccessListener {
                // User profile has been successfully update.
                Log.i(
                    fragment.javaClass.simpleName,
                    fragment.resources.getString(R.string.update_profile_success)
                )
                fragment.showInfoSnackBar(fragment.resources.getString(R.string.update_profile_success))
                fragment.profileUpdateSuccess()
            }
            .addOnFailureListener { e ->
                // hide progress bar and show error
                val profilePB: ProgressBar = fragment.requireView().findViewById(R.id.profilePB)
                profilePB.visibility = View.INVISIBLE
                Log.e(
                    fragment.javaClass.simpleName,
                    fragment.resources.getString(R.string.update_profile_error),
                    e
                )
                fragment.showErrorSnackBar(fragment.resources.getString(R.string.update_profile_error))
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
