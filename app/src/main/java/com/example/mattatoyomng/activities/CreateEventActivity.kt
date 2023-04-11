package com.example.mattatoyomng.activities

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.mattatoyomng.R
import com.example.mattatoyomng.databinding.ActivityCreateEventBinding
import com.example.mattatoyomng.models.Event
import com.example.mattatoyomng.models.User
import com.example.mattatoyomng.showSnackbar
import com.example.mattatoyomng.stripSpaces
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*


class CreateEventActivity : AppCompatActivity() {

    private val TAG: String = "CreateEventActivity"

    private lateinit var binding: ActivityCreateEventBinding
    private lateinit var layout: View

    // user credentials
    private var currentUserID: String = ""
    private var currentUserName: String = ""

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private var storageReference = FirebaseStorage.getInstance().reference
    private var db = FirebaseFirestore.getInstance()
    private var collectionReference: CollectionReference = db.collection("events")

    // Global variable for URI of a selected image from phone storage.
    private var eventImageUri: Uri? = null

    // Global variable for a event image URL
    private var eventImageUrl: String = ""

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.apply {

            // bind layout
            layout = createEventLayout

            // hide progress bar
            createEventPB.visibility = View.INVISIBLE

            // set event owner to current user
//            if (User.instance != null) {
//                currentUserID = User.instance!!.userid.toString()
//                currentUserName = User.instance!!.name.toString()
//            }
//            ownerNameTV.text = currentUserName

            // upload image
            addEventImageTV.setOnClickListener() {
                requestStoragePermission(view = it)
            }

            // save event
            saveEventBTN.setOnClickListener() {
                saveEvent()
            }
        }
    }

    // ActivityResultLauncher to open gallery
    private val openGalleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // get the returned result from the lambda and check the resultCode and the data returned
            // if the data is not null reference the imageView from the layout
            if (result.resultCode == RESULT_OK && result.data != null) {
                val eventImage: ImageView = findViewById(R.id.showEventImageIV)
                val addEventImageTV: TextView = findViewById(R.id.addEventImageTV)
                // show image uploaded
                eventImage.setImageURI(result.data?.data)
                // change "add image" button text
                addEventImageTV.setText(R.string.update_event_image)
                // save ImageUri for Firebase Storage upload
                eventImageUri = result.data?.data!!
            }
        }

    // ActivityResultLauncher for MultiplePermissions
    private val requestPermissionsLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                val permissionName = it.key
                val isGranted = it.value
                // if permission is granted perform operation
                if (isGranted) {
                    // If permission name is READ_MEDIA_IMAGES, call gallery launcher with pick intent
                    if (permissionName == Manifest.permission.READ_MEDIA_IMAGES) {
                        val pickIntent =
                            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        openGalleryLauncher.launch(pickIntent)
                    }
                } else {
                    // Displaying toast if storage permission is not granted
                    if (permissionName == Manifest.permission.READ_MEDIA_IMAGES)
                        Toast.makeText(
                            this@CreateEventActivity,
                            R.string.permission_storage_required,
                            Toast.LENGTH_LONG
                        ).show()
                }
            }
        }

    // Method to request storage permission
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestStoragePermission(view: View) {
        // Check if the permission was denied and show rationale
        if (
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            )
        ) {
            // call the rationale dialog to tell the user why they need to allow permission request
            layout.showSnackbar(
                view,
                getString(R.string.permission_storage_required),
                Snackbar.LENGTH_INDEFINITE,
                null
            ) {}
        } else {
            // If it has not been denied then request, directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            requestPermissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES
                )
            )
        }
    }

    // Method to get the extension of selected image.
    private fun getFileExtension(uri: Uri?): String? {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri!!))
    }

    // Method to upload event image to firebase storage. Also sets image URI global var
    private fun uploadEventImage() {
        val imageFilename =
            "event_" + System.currentTimeMillis() + "." + getFileExtension(eventImageUri)

        if (eventImageUri != null) {
            // save image to Firebase Storage "events_images" folder
            val sRef: StorageReference = storageReference
                .child("events_images")
                .child(imageFilename)
            sRef.putFile(eventImageUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    Log.i(
                        TAG,
                        "Event image URL = ${taskSnapshot.metadata!!.reference!!.downloadUrl}"
                    )
                    // Get the downloadable url from the task snapshot
                    // assign value to image URL global variable
                    taskSnapshot.metadata!!.reference!!.downloadUrl
                        .addOnSuccessListener { uri ->
                            Log.i("Downloadable Image URL", uri.toString())
                            eventImageUrl = uri.toString()
                        }
                }
                .addOnFailureListener { exception ->
                    // if operation fails show toast with error
                    Log.e(TAG, "ERROR: ${exception.message}")
                    Toast.makeText(
                        this@CreateEventActivity,
                        exception.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    // Method to save event:
    // 1. upload event image to Storage
    // 2. create Event object
    // 3. add new event to Firestore collection
    private fun saveEvent() {
        // get event info
        val title: String = binding.eventTitleET.text.toString()
        val description: String = binding.eventDescriptionET.text.toString()
        val owner: String = currentUserName
        val date = Timestamp(Date())

        // first upload image to firebase storage
        uploadEventImage()

        // title must not be empty to create event
        if (!TextUtils.isEmpty(title)) {
            // make progress bar visible
            binding.createEventPB.visibility = View.VISIBLE

            val newEvent = Event(
                title,
                description,
                owner,
                date,
                eventImageUri.toString()
            )
            collectionReference.add(newEvent)
                .addOnSuccessListener {
                    // if operation successful:
                    // 1. hide progress bar
                    binding.createEventPB.visibility = View.INVISIBLE
                    // 2. go back to main activity
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
                .addOnFailureListener {
                    // if operation fails:
                    // 1. hide progress bar
                    binding.createEventPB.visibility = View.INVISIBLE
                    // 2. show Toast with error
                    Log.e(TAG, "ERROR: ${it.message.toString()}")
                    Toast.makeText(
                        this,
                        "ERROR: ${it.message.toString()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            Toast.makeText(this, "Event title cannot be empty", Toast.LENGTH_SHORT).show()
        }
    }
}