package com.example.mattatoyomng.firebase

import android.net.Uri
import android.util.Log
import android.view.View
import com.example.mattatoyomng.coroutines.CoroutineScopes
import com.example.mattatoyomng.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.launch

class FirebaseStorageClass {
    private val storageReference = FirebaseStorage.getInstance().reference

    interface UploadImageCallback {
        fun onUploadImageSuccess(url: String)
        fun onUploadImageFail(e: Exception)
    }

    fun uploadImage(callback: UploadImageCallback, filename: String, imageUri: Uri) {
        CoroutineScopes.IO.launch {
            storageReference
                .child(Constants.USER_PICS_FOLDER)
                .child(filename)
                .putFile(imageUri)
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.metadata!!.reference!!.downloadUrl
                        .addOnSuccessListener { uri ->
                            val imageUrl = uri.toString()
                            callback.onUploadImageSuccess(imageUrl)
                        }
                }
                .addOnFailureListener { e ->
                    callback.onUploadImageFail(e)
                }
        }
    }

    fun uploadEventImage(callback: UploadImageCallback, filename: String, imageUri: Uri) {
        CoroutineScopes.IO.launch {
            storageReference
                .child(Constants.EVENT_IMAGES)
                .child(filename)
                .putFile(imageUri)
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.metadata!!.reference!!.downloadUrl
                        .addOnSuccessListener { uri ->
                            val imageUrl = uri.toString()
                            callback.onUploadImageSuccess(imageUrl)
                        }
                }
                .addOnFailureListener { e ->
                    callback.onUploadImageFail(e)
                }
        }
    }

//    val sRef: StorageReference = storageReference
//        .child("events_images")
//        .child(imageFilename)
//    sRef.putFile(eventImageUri!!)
//    .addOnSuccessListener
//    {
//        taskSnapshot ->
//        // hide progress bar
//        binding.createEventPB.visibility = View.INVISIBLE
//        // Get the downloadable url from the task snapshot
//        // assign value to image URL global variable
//        taskSnapshot.metadata!!.reference!!.downloadUrl
//            .addOnSuccessListener { uri ->
//                eventImageUrl = uri.toString()
//                Log.i("Downloadable Image URL", eventImageUrl)
//                saveEvent()
//            }
//    }
//    .addOnFailureListener
//    {
//        exception ->
//        // if operation fails show snackbar with error
//        val msg = "ERROR: ${exception.message}"
//        Log.e(TAG, msg)
//        showErrorSnackBar(msg)
//    }
}