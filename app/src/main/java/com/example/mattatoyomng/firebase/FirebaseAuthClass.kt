package com.example.mattatoyomng.firebase

import com.example.mattatoyomng.coroutines.CoroutineScopes
import com.example.mattatoyomng.models.User
import com.example.mattatoyomng.utils.logThread
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FirebaseAuthClass {

    private val TAG = "FirebaseAuthClass"

    private val firebaseAuth = FirebaseAuth.getInstance()

    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    // Function to get the userid of current logged user.
    // Return empty string if no user is logged in.
    fun getCurrentUserID(): String {
        val currentUser = getCurrentUser()
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
                        username
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
}
//        firebaseAuth.signInWithEmailAndPassword(email, password)
//            .addOnCompleteListener(this) { task ->
//                if (task.isSuccessful) {
//                    // Sign in success, update UI with the signed-in user's information
//                    Log.d(TAG, "signInWithEmail:success")
//                    FirestoreClass()
//                        .loadUserData(this@LoginActivity)
//                } else {
//                    // If sign in fails, display a message to the user.
//                    Log.w(TAG, "signInWithEmail:failure", task.exception)
//                    showErrorSnackBar(
//                        "${resources.getString(R.string.auth_failed)}: " +
//                                "${task.exception!!.message}"
//                    )
//
//                }
//
//            }