package com.example.mattatoyomng.firebase

import android.app.Activity
import android.util.Log
import androidx.fragment.app.Fragment
import com.example.mattatoyomng.activities.*
import com.example.mattatoyomng.coroutines.CoroutineScopes
import com.example.mattatoyomng.fragments.EventsFragment
import com.example.mattatoyomng.fragments.UpdatePasswordFragment
import com.example.mattatoyomng.fragments.UpdateProfileFragment
import com.example.mattatoyomng.models.Event
import com.example.mattatoyomng.models.User
import com.example.mattatoyomng.utils.Constants
import com.google.firebase.Timestamp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.*
import java.util.*

class FirestoreClass {

    private val TAG = "FirestoreClass"

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
                        is EventCreateUpdateActivity -> {
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
                        is EventCreateUpdateActivity -> {
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
                fragment.profileUpdateSuccess()
            }
            .addOnFailureListener { e ->
                // hide progress bar and show error
                fragment.profileUpdateFail(e)
            }
    }

    interface CreateEventCallback {
        fun onCreateEventSuccess()
        fun onCreateEventError(e: Exception)
    }

    fun createEvent(callback: CreateEventCallback, event: Event) {
        CoroutineScopes.IO.launch {
            dbFirestore.collection(Constants.EVENTS)
                .document()
                .set(event, SetOptions.merge())
                .addOnSuccessListener {
                    callback.onCreateEventSuccess()
                }
                .addOnFailureListener{e ->
                    callback.onCreateEventError(e)
                }
        }
    }

    interface UpdateEventCallback {
        fun onUpdateEventSuccess()
        fun onUpdateEventError(e: Exception)
    }


    fun updateEvent(
        callback: UpdateEventCallback,
        eventHashMap: HashMap<String, Any?>,
        documentId: String,
    ) {
        CoroutineScopes.IO.launch {
            dbFirestore.collection(Constants.EVENTS)
                .document(documentId)
                .update(eventHashMap)
                .addOnSuccessListener {
                    callback.onUpdateEventSuccess()
                }
                .addOnFailureListener {e ->
                    callback.onUpdateEventError(e)
                }
        }
    }

    interface GetEventListCallback {
        fun onGetEventListResult(eventList: MutableList<Event>)
    }

    fun getEventsList(callback: GetEventListCallback) {
        CoroutineScopes.IO.launch {
            // define today
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            val today = Timestamp(cal.time)
            // get events after today (included) and order them by date
            val storageRef = dbFirestore.collection(Constants.EVENTS)
                .whereGreaterThanOrEqualTo("date", today)
                .orderBy("date")
            storageRef.get()
                .addOnSuccessListener { documents ->
                    val eventList: MutableList<Event> = mutableListOf()
                    if (!documents.isEmpty) {
                        documents.forEach {
                            // convert snapshots to Event objects
                            val event = it.toObject(Event::class.java)
                            event.documentId = it.id
                            eventList.add(event)
                        }
                    }
                    callback.onGetEventListResult(eventList)
                }
                .addOnFailureListener {
                    val msg = "ERROR: ${it.message}"
                    Log.e(TAG, msg)
                    callback.onGetEventListResult(mutableListOf())
                }
        }
    }

    interface DeleteEventCallback {
        fun onDeleteEventSuccess(documentId: String)
        fun onDeleteEventFail(e: Exception)
    }

    fun deleteEvent(callback: DeleteEventCallback, documentId: String) {
        CoroutineScopes.IO.launch {
            dbFirestore.collection(Constants.EVENTS)
                .document(documentId)
                .delete()
                .addOnSuccessListener {
                    callback.onDeleteEventSuccess(documentId)
                }
                .addOnFailureListener { e ->
                    callback.onDeleteEventFail(e)
                }

        }
    }

    fun searchInFirestore(fragment: EventsFragment, searchText: String) {
        dbFirestore.collection(Constants.EVENTS)
            .orderBy(Constants.KEYWORDS)
            .startAt(searchText)
            .endAt("$searchText\uf8ff")
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(TAG, "searchtext = $searchText")
                    Log.d(TAG, "event list size ${it.result.size()}")
                    val eventList: ArrayList<Event> =
                        it.result!!.toObjects(Event::class.java) as ArrayList<Event>
                    fragment.setupEventsRecyclerView(eventList)
                } else {
                    fragment.showErrorSnackBar("Error: ${it.exception!!.message}")
                }
            }

    }

    fun updateUserPassword(fragment: UpdatePasswordFragment, currentPassword: String, newPassword: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser!!
        val credential = EmailAuthProvider.getCredential(currentUser.email.toString(), currentPassword)
        currentUser.reauthenticate(credential)
            .addOnSuccessListener {
                fragment.authenticationSuccess()
                currentUser.updatePassword(newPassword)
                    .addOnSuccessListener {
                        fragment.updatePasswordSuccess()
                    }
                    .addOnFailureListener {e ->
                        fragment.updatePasswordFail(e)
                    }
            }
            .addOnFailureListener {e ->
                fragment.authenticationFail(e)
            }
    }

}
