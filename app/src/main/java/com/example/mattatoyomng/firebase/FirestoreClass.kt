package com.example.mattatoyomng.firebase

import android.app.Activity
import android.util.Log
import androidx.fragment.app.Fragment
import com.example.mattatoyomng.activities.EventCreateUpdateActivity
import com.example.mattatoyomng.activities.LoginActivity
import com.example.mattatoyomng.activities.MainActivity
import com.example.mattatoyomng.coroutines.CoroutineScopes
import com.example.mattatoyomng.fragments.UpdatePasswordFragment
import com.example.mattatoyomng.fragments.UpdateProfileFragment
import com.example.mattatoyomng.models.Event
import com.example.mattatoyomng.models.User
import com.example.mattatoyomng.utils.Constants
import com.example.mattatoyomng.utils.getTodayTimestamp
import com.example.mattatoyomng.utils.logThread
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch

class FirestoreClass {

    private val TAG = "FirestoreClass"

    private val dbFirestore = FirebaseFirestore.getInstance()

    interface RegisterUserCallback {
        fun onRegisterUserSuccess()
        fun onRegisterUserError(e: Exception)
    }

    // Register user by creating an entry in Firestore collection "users"
    fun registerUser(callback: RegisterUserCallback, userInfo: User) {
        CoroutineScopes.IO.launch {
            logThread("registerUser() in FirestoreClass")
            dbFirestore.collection(Constants.USERS)
                .document(FirebaseAuthClass().getCurrentUserID())
                .set(userInfo, SetOptions.merge())
                .addOnSuccessListener {
                    callback.onRegisterUserSuccess()
                }
                .addOnFailureListener { e ->
                    callback.onRegisterUserError(e)
                }
        }
    }

    interface GetUserDataCallback {
        fun onGetUserDataSuccess(user: User)
        fun onGetUserDataFail(e: Exception)
    }

    fun loadUserData(callback: GetUserDataCallback) {
        CoroutineScopes.IO.launch {
            dbFirestore.collection(Constants.USERS)
                .document(FirebaseAuthClass().getCurrentUserID())
                .get()
                .addOnSuccessListener { document ->
                    // Convert document to User object.
                    val loggedInUser = document.toObject(User::class.java)!!
                    callback.onGetUserDataSuccess(loggedInUser)
                }
                .addOnFailureListener { e ->
                    callback.onGetUserDataFail(e)
                }
        }
    }

    // Function to update user profile using hashmap
    fun updateUserProfileData(fragment: UpdateProfileFragment, userHashMap: HashMap<String, Any>) {
        dbFirestore.collection(Constants.USERS)
            .document(FirebaseAuthClass().getCurrentUserID())
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
                .addOnFailureListener { e ->
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
                .addOnFailureListener { e ->
                    callback.onUpdateEventError(e)
                }
        }
    }

    interface DeleteEventCallback {
        fun onDeleteEventSuccess(position: Int)
        fun onDeleteEventFail(e: Exception)
    }

    fun deleteEvent(callback: DeleteEventCallback, documentId: String, position: Int) {
        CoroutineScopes.IO.launch {
            dbFirestore.collection(Constants.EVENTS)
                .document(documentId)
                .delete()
                .addOnSuccessListener {
                    callback.onDeleteEventSuccess(position)
                }
                .addOnFailureListener { e ->
                    callback.onDeleteEventFail(e)
                }
        }
    }

    interface GetEventListCallback {
        fun onGetEventListResult(eventList: MutableList<Event>)
    }

    fun getEventsList(callback: GetEventListCallback) {
        CoroutineScopes.IO.launch {
            val today = getTodayTimestamp()
            // get events after today (included) and order them by date
            val storageRef = dbFirestore.collection(Constants.EVENTS)
                .whereGreaterThanOrEqualTo("date", today)
                .orderBy("date")
            storageRef.get()
                .addOnSuccessListener { documents ->
                    val eventList: MutableList<Event> = mutableListOf()
                    if (!documents.isEmpty) {
                        documents.forEachIndexed { index, it ->
                            // convert snapshots to Event objects
                            val event = it.toObject(Event::class.java)
                            event.documentId = it.id
                            eventList.add(event)
                            Log.d(
                                TAG, "position = $index, " +
                                        "id = ${event.documentId}, " +
                                        "title = ${event.title}"
                            )
                        }
                    }
                    callback.onGetEventListResult(eventList)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "ERROR GETTING EVENTS LIST: $e")
                    callback.onGetEventListResult(mutableListOf())
                }
        }
    }

    interface GetEventListSearchCallback {
        fun onGetEventListSearch(eventList: MutableList<Event>)
    }

    fun searchInFirestore(callback: GetEventListSearchCallback, searchText: String) {
        CoroutineScopes.IO.launch {
            dbFirestore.collection(Constants.EVENTS)
                .orderBy(Constants.KEYWORDS)
                .startAt(searchText)
                .endAt("$searchText\uf8ff")
                .get()
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
                    callback.onGetEventListSearch(eventList)
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "ERROR SEARCHING FOR EVENTS: $e")
                    callback.onGetEventListSearch(mutableListOf())
                }
        }
    }

    fun updateUserPassword(
        fragment: UpdatePasswordFragment,
        currentPassword: String,
        newPassword: String
    ) {
        val currentUser = FirebaseAuthClass().getCurrentUser()!!
        val credential =
            EmailAuthProvider.getCredential(currentUser.email.toString(), currentPassword)
        currentUser.reauthenticate(credential)
            .addOnSuccessListener {
                fragment.authenticationSuccess()
                currentUser.updatePassword(newPassword)
                    .addOnSuccessListener {
                        fragment.updatePasswordSuccess()
                    }
                    .addOnFailureListener { e ->
                        fragment.updatePasswordFail(e)
                    }
            }
            .addOnFailureListener { e ->
                fragment.authenticationFail(e)
            }
    }

}
