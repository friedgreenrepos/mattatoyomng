package com.example.mattatoyomng.firebase

import android.app.Activity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.mattatoyomng.R
import com.example.mattatoyomng.activities.CreateEventActivity
import com.example.mattatoyomng.activities.LoginActivity
import com.example.mattatoyomng.activities.MainActivity
import com.example.mattatoyomng.activities.RegisterActivity
import com.example.mattatoyomng.fragments.EventsFragment
import com.example.mattatoyomng.fragments.TAG
import com.example.mattatoyomng.fragments.UpdateProfileFragment
import com.example.mattatoyomng.models.Event
import com.example.mattatoyomng.models.User
import com.example.mattatoyomng.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirestoreClass {

    private val dbFirestore = FirebaseFirestore.getInstance()

    // Function to get the userid of current logged user. Return empty string if no user is logged in.
    fun getCurrentUserID(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID = ""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }
        return currentUserID
    }

    // Register user by creating an entry in Firestore collection "users"
    fun registerUser(activity: RegisterActivity, userInfo: User) {

        dbFirestore.collection(Constants.USERS)
            // Get document for current user
            .document(getCurrentUserID())
            // merge User info with user document
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }
            .addOnFailureListener { e ->
                val msg = "Error writing document: ${e.message}"
                Log.e(activity.javaClass.simpleName, msg)
                activity.showErrorSnackBar(msg)
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

    fun createEvent(activity: CreateEventActivity, event: Event) {
        dbFirestore.collection(Constants.EVENTS)
            .document()
            .set(event, SetOptions.merge())
            .addOnSuccessListener {
                val msg = activity.resources.getString(R.string.create_event_success)
                activity.eventUploadSuccess(msg)
            }
            .addOnFailureListener {
                val msg = activity.resources.getString(R.string.create_event_fail) +
                        "ERROR: ${it.message.toString()}"
                activity.eventUploadFail(msg)
            }
    }

    fun updateEvent(
        activity: CreateEventActivity,
        eventHashMap: HashMap<String, Any?>,
        documentId: String
    ) {
        dbFirestore.collection(Constants.EVENTS)
            .document(documentId)
            .update(eventHashMap)
            .addOnSuccessListener {
                val msg = activity.resources.getString(R.string.update_event_success)
                activity.eventUploadSuccess(msg)
            }
            .addOnFailureListener {
                val msg = activity.resources.getString(R.string.update_event_fail) +
                        "ERROR: ${it.message.toString()}"
                activity.eventUploadFail(msg)
            }
    }

    fun getEventsList(fragment: EventsFragment) {
        // get events ordered by date
        val docRef = dbFirestore.collection(Constants.EVENTS).orderBy("date")
        docRef.get()
            .addOnSuccessListener { document ->
                Log.d(TAG, "n.of events in db = ${document.size()}")
                val eventList: ArrayList<Event> = ArrayList()
                if (!document.isEmpty) {
                    document.forEach {
                        // convert snapshots to Event objects
                        val event = it.toObject(Event::class.java)
                        event.documentId = it.id
                        eventList.add(event)
                    }
                }
                fragment.setupEventsRecyclerView(eventList)
            }
            .addOnFailureListener {
                val msg = "ERROR: ${it.message}"
                Log.e(TAG, msg)
                fragment.showErrorSnackBar(msg)
            }
    }

    fun deleteEvent(fragment: EventsFragment, documentId: String){
        dbFirestore.collection(Constants.EVENTS)
            .document(documentId)
            .delete()
            .addOnSuccessListener {
                fragment.showInfoSnackBar(fragment.resources.getString(R.string.delete_event_success))
            }
            .addOnFailureListener{e ->
                fragment.showErrorSnackBar(
                    fragment.resources.getString(R.string.delete_event_fail) + ":" + e
                )
            }
    }

}
