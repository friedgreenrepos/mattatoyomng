package com.example.mattatoyomng.firebase

import com.example.mattatoyomng.coroutines.CoroutineScopes
import com.example.mattatoyomng.models.User
import com.example.mattatoyomng.utils.logThread
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class FirebaseAuthClass {

    private val TAG = "FirebaseAuthClass"

    private val firebaseAuth = FirebaseAuth.getInstance()

    private fun getCurrentFirebaseUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    // Function to get the userid of current logged user.
    // Return empty string if no user is logged in.
    fun getCurrentUserID(): String {
        val currentUser = getCurrentFirebaseUser()
        var currentUserID = ""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }
        return currentUserID
    }

    interface CreateUserCallback {
        fun onCreateUserSuccess(user: User)
        fun onCreateUserError(e: Exception)
    }


    fun createUser(
        callback: CreateUserCallback,
        email: String,
        password: String,
        name: String,
        username: String,
    ) {
        CoroutineScopes.IO.launch {
            logThread("createUser() in FirebaseAuthClass")
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    val firebaseUser: FirebaseUser = authResult.user!!
                    firebaseUser.uid
                    val user = User(
                        firebaseUser.uid,
                        name,
                        username,
                        email
                    )
                    callback.onCreateUserSuccess(user)
                }
                .addOnFailureListener { e ->
                    callback.onCreateUserError(e)
                }
        }
    }

    interface LoginUserCallback {
        fun onLoginSuccess()
        fun onLoginFail(e: Exception)
    }

    fun loginUser(callback: LoginUserCallback, email: String, password: String) {
        CoroutineScopes.IO.launch {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    callback.onLoginSuccess()
                }
                .addOnFailureListener { e ->
                    callback.onLoginFail(e)
                }
        }
    }

    interface ReAuthenticateCallback {
        fun onReAuthenticateSuccess(currentUser: FirebaseUser, newPassword: String)
        fun onReAuthenticateFail(e: Exception)
    }

    fun reAuthenticateUser(
        callback: ReAuthenticateCallback,
        password: String,
        newPassword: String
    ) {
        CoroutineScopes.IO.launch {
            val currentUser = getCurrentFirebaseUser()!!
            val credential =
                EmailAuthProvider.getCredential(currentUser.email.toString(), password)
            currentUser.reauthenticate(credential)
                .addOnSuccessListener {
                    callback.onReAuthenticateSuccess(currentUser, newPassword)
                }
                .addOnFailureListener {e ->
                    callback.onReAuthenticateFail(e)
                }
        }
    }
}
