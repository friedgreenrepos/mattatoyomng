package com.example.mattatoyomng.firebase

import android.util.Log
import com.example.mattatoyomng.coroutines.CoroutineScopes
import com.example.mattatoyomng.models.Event
import com.example.mattatoyomng.models.Todo
import com.example.mattatoyomng.models.User
import com.example.mattatoyomng.utils.Constants
import com.example.mattatoyomng.utils.getTodayTimestamp
import com.example.mattatoyomng.utils.logThread
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch

class FirestoreClass {

    private val TAG = "FirestoreClass"

    val dbFirestore = FirebaseFirestore.getInstance()

    interface IsUserAdminCallback {
        fun isUserAdminSuccess(isAdmin: Boolean)
        fun isUserAdminFail(e: Exception)
    }
    fun isCurrentUserAdmin(callback: IsUserAdminCallback) {
        CoroutineScopes.IO.launch {
            dbFirestore.collection(Constants.USERS)
                .document(FirebaseAuthClass().getCurrentUserID())
                .get()
                .addOnSuccessListener { document ->
                    // Convert document to User object.
                    val loggedInUser = document.toObject(User::class.java)!!
                    val isAdmin = loggedInUser.admin

                    Log.d(TAG, "is user ${loggedInUser.username} admin?? $isAdmin")
                    callback.isUserAdminSuccess(isAdmin)
                }
                .addOnFailureListener { e ->
                    callback.isUserAdminFail(e)
                }
        }
    }


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

    fun getUserData(callback: GetUserDataCallback) {
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

    interface UpdateProfileCallback {
        fun onUpdateProfileSuccess()
        fun onUpdateProfileFail(e: Exception)
    }

    // Function to update user profile using hashmap
    fun updateUserProfileData(callback: UpdateProfileCallback, userHashMap: HashMap<String, Any>) {
        CoroutineScopes.IO.launch {
            dbFirestore.collection(Constants.USERS)
                .document(FirebaseAuthClass().getCurrentUserID())
                .update(userHashMap)
                .addOnSuccessListener {
                    callback.onUpdateProfileSuccess()
                }
                .addOnFailureListener { e ->
                    callback.onUpdateProfileFail(e)
                }
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

    interface UpdateUserPasswordCallback {
        fun onUpdatePasswordSuccess()
        fun onUpdatePasswordFail(e: Exception)
    }

    fun updateUserPassword(
        callback: UpdateUserPasswordCallback,
        currentUser: FirebaseUser,
        newPassword: String
    ) {
        CoroutineScopes.IO.launch {
            currentUser.updatePassword(newPassword)
                .addOnSuccessListener {
                    callback.onUpdatePasswordSuccess()
                }
                .addOnFailureListener { e ->
                    callback.onUpdatePasswordFail(e)
                }
        }
    }

    interface CreateTodoCallback {
        fun onCreateTodoSuccess()
        fun onCreateTodoError(e: Exception)
    }

    fun createTodo(callback: CreateTodoCallback, todo: Todo) {
        CoroutineScopes.IO.launch {
            dbFirestore.collection(Constants.TODOS)
                .document()
                .set(todo, SetOptions.merge())
                .addOnSuccessListener {
                    callback.onCreateTodoSuccess()
                }
                .addOnFailureListener { e ->
                    callback.onCreateTodoError(e)
                }
        }
    }

    interface DeleteTodoCallback {
        fun onDeleteTodoSuccess(position: Int)
        fun onDeleteTodoFail(e: Exception)
    }

    fun deleteTodo(callback: DeleteTodoCallback, documentId: String, position: Int) {
        CoroutineScopes.IO.launch {
            dbFirestore.collection(Constants.TODOS)
                .document(documentId)
                .delete()
                .addOnSuccessListener {
                    callback.onDeleteTodoSuccess(position)
                }
                .addOnFailureListener { e ->
                    callback.onDeleteTodoFail(e)
                }
        }
    }

    interface GetTodoListCallback {
        fun onGetTodoListResult(todoList: MutableList<Todo>)
    }

    fun getTodoList(callback: GetTodoListCallback) {
        CoroutineScopes.IO.launch {
            dbFirestore.collection(Constants.TODOS)
                .orderBy("dateAdded", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { documents ->
                    val todoList: MutableList<Todo> = mutableListOf()
                    if (!documents.isEmpty) {
                        documents.forEach {
                            val todo = it.toObject(Todo::class.java)
                            // assign id to each todo item
                            todo.documentId = it.id
                            todoList.add(todo)
                        }
                    }
                    callback.onGetTodoListResult(todoList)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "ERROR GETTING TODO LIST: $e")
                    callback.onGetTodoListResult(mutableListOf())
                }
        }
    }

    interface UpdateTodoCallback {
        fun onUpdateTodoSuccess()
        fun onUpdateTodoFail(e: Exception)
    }

    fun updateTodoStatus(callback: UpdateTodoCallback, documentId: String, isChecked: Boolean) {
        CoroutineScopes.IO.launch {
            dbFirestore.collection(Constants.TODOS)
                .document(documentId)
                .update("done", isChecked)
                .addOnSuccessListener {
                    callback.onUpdateTodoSuccess()
                }
                .addOnFailureListener { e ->
                    callback.onUpdateTodoFail(e)
                }
        }
    }

}
