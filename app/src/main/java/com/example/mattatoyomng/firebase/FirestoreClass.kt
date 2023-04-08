package com.example.mattatoyomng.firebase

import android.util.Log
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
            // Document ID for users fields. Here the document it is the User ID.
            .document(getCurrentUserID())
            // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {

                // Here call a function of base activity for transferring the result to it.
                activity.userRegisterSuccess()
            }
            .addOnFailureListener { e ->
//                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error writing document",
                    e
                )
            }
    }

    /**
     * A function for getting the user id of current logged user.
     */
    fun getCurrentUserID(): String {
        // An Instance of currentUser using FirebaseAuth
        val currentUser = FirebaseAuth.getInstance().currentUser

        // A variable to assign the currentUserId if it is not null or else it will be blank.
        var currentUserID = ""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }

        return currentUserID
    }
}