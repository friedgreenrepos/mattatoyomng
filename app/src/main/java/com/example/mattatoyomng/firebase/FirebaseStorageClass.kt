package com.example.mattatoyomng.firebase

import android.net.Uri
import com.example.mattatoyomng.coroutines.CoroutineScopes
import com.example.mattatoyomng.utils.Constants.USER_PICS_FOLDER
import com.google.firebase.storage.FirebaseStorage
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
                .child(USER_PICS_FOLDER)
                .child(filename)
                .putFile(imageUri)
                .addOnSuccessListener {taskSnapshot ->
                    taskSnapshot.metadata!!.reference!!.downloadUrl
                        .addOnSuccessListener {uri ->
                            val imageUrl = uri.toString()
                            callback.onUploadImageSuccess(imageUrl)
                        }
                }
                .addOnFailureListener { e ->
                    callback.onUploadImageFail(e)
                }
        }
    }
}