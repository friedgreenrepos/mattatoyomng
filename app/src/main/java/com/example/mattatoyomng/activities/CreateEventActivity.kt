package com.example.mattatoyomng.activities

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.example.mattatoyomng.R
import com.example.mattatoyomng.databinding.ActivityCreateEventBinding
import com.example.mattatoyomng.firebase.FirestoreClass
import com.example.mattatoyomng.fragments.EventsFragment
import com.example.mattatoyomng.models.Event
import com.example.mattatoyomng.models.User
import com.example.mattatoyomng.utils.*
import com.google.android.material.button.MaterialButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class CreateEventActivity : BaseActivity(), View.OnClickListener {

    val TAG: String = "CreateEventActivity"

    // binding
    private lateinit var binding: ActivityCreateEventBinding

    // Toolbar
    private lateinit var toolbar: Toolbar

    // Firebase
    private lateinit var auth: FirebaseAuth
    private var storageReference = FirebaseStorage.getInstance().reference

    // Global variable for URI of a selected image from phone storage.
    private var eventImageUri: Uri? = null

    // Global variable for URL of a selected image from phone storage.
    private var eventImageUrl: String = ""

    // calendar for date/time picker
    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private lateinit var timeSetListener: TimePickerDialog.OnTimeSetListener

    // event passed in intent
    private var eventDetails: Event? = null

    // tag list
    private var eventTagsList: MutableList<String> = arrayListOf()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // initialize date picker listener
        dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }

        // initialize time picker listener
        timeSetListener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
            cal.set(Calendar.MINUTE, minute)
            updateTimeInView()
        }

        // check if event is passed in intent
        if (intent.hasExtra(EventsFragment.EVENT_DETAILS)) {
            eventDetails =
                intent.getParcelableExtra(EventsFragment.EVENT_DETAILS, Event::class.java)
        }

        // set-up layout:
        // if eventDetails exists -> create event
        // else -> edit event
        binding.apply {
            toolbar = toolbarCreateEvent
            eventDateTV.setOnClickListener(this@CreateEventActivity)
            eventTimeTV.setOnClickListener(this@CreateEventActivity)
            // upload image
            addEventImageTV.setOnClickListener(this@CreateEventActivity)
            // save/update event
            saveEventBTN.setOnClickListener(this@CreateEventActivity)
            // add tags
            addTagLL.setOnClickListener(this@CreateEventActivity)

            // load event data if event details is passed in intent
            if (eventDetails != null) {
                setupActionBarEdit()
                eventTitleET.setText(eventDetails!!.title)
                eventDescriptionET.setText(eventDetails!!.description)
                eventDateTV.text = dateFormatter(eventDetails!!.date)
                eventTimeTV.text = timeFormatter(eventDetails!!.date)
                ownerNameTV.text = eventDetails!!.owner
                eventTagsList = eventDetails!!.tags
                addTags(eventTagsList)
                saveEventBTN.text = resources.getString(R.string.update)
                try {
                    Glide
                        .with(this@CreateEventActivity)
                        .load(eventDetails!!.eventImgURL)
                        .centerCrop()
                        .placeholder(R.drawable.image_red_transparency4)
                        .into(showEventImageIV)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.e(TAG, "Error loading event image: ${e.message}")
                }
            } else {
                setupActionBarCreate()
                // populate date TV with today's date and time TV with current time
                updateDateInView()
                updateTimeInView()
                // set event owner to current user
                FirestoreClass().loadUserData(this@CreateEventActivity)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.eventDateTV -> {
                DatePickerDialog(
                    this@CreateEventActivity,
                    dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
            R.id.eventTimeTV -> {
                TimePickerDialog(
                    this@CreateEventActivity,
                    timeSetListener,
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    DateFormat.is24HourFormat(this@CreateEventActivity)
                ).show()
            }
            R.id.addEventImageTV -> {
                requestStoragePermission(view = v)
            }
            R.id.saveEventBTN -> {
                if (eventImageUri != null) uploadEventImage()
                else saveEvent()
            }
            R.id.addTagLL ->{
                showAddTagDialog()
            }
        }
    }

    private fun updateDateInView() {
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding.eventDateTV.text = sdf.format(cal.time).toString()
    }

    private fun updateTimeInView() {
        val myFormat = "hh:mm"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding.eventTimeTV.text = sdf.format(cal.time).toString()
    }

    private fun setupActionBarCreate() {
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar

        // add back button and back navigation functionality and remove title
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.arrow_back_white)
            actionBar.title = "Create event"
        }
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupActionBarEdit() {
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar

        // add back button and back navigation functionality and remove title
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.arrow_back_white)
            actionBar.title = "Edit event"
        }
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    // ActivityResultLauncher to open gallery
    private val openGalleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // get the returned result from the lambda and check the resultCode and the data returned
            // if the data is not null reference the imageView from the layout
            if (result.resultCode == RESULT_OK && result.data != null) {
                val eventImage: ImageView = binding.showEventImageIV
                val addEventImageTV: TextView = binding.addEventImageTV
                eventImageUri = result.data?.data!!
                // show selected image
                try {
                    Glide
                        .with(this@CreateEventActivity)
                        .load(Uri.parse(eventImageUri.toString()))
                        .centerCrop()
                        .placeholder(R.drawable.user_white_80)
                        .into(eventImage)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.e(TAG, "Error loading event image: ${e.message}")
                }
//                eventImage.setImageURI(result.data?.data)
                // change "add image" button text
                addEventImageTV.setText(R.string.update_event_image)
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
            showInfoSnackBar(getString(R.string.permission_storage_required))
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
        // show progress bar
        binding.createEventPB.visibility = View.VISIBLE

        val imageFilename =
            "event_" + System.currentTimeMillis() + "." + getFileExtension(eventImageUri)

        if (eventImageUri != null) {
            // save image to Firebase Storage "events_images" folder
            val sRef: StorageReference = storageReference
                .child("events_images")
                .child(imageFilename)
            sRef.putFile(eventImageUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    // hide progress bar
                    binding.createEventPB.visibility = View.INVISIBLE
                    // Get the downloadable url from the task snapshot
                    // assign value to image URL global variable
                    taskSnapshot.metadata!!.reference!!.downloadUrl
                        .addOnSuccessListener { uri ->
                            eventImageUrl = uri.toString()
                            Log.i("Downloadable Image URL", eventImageUrl)
                            saveEvent()
                        }
                }
                .addOnFailureListener { exception ->
                    // if operation fails show snackbar with error
                    val msg = "ERROR: ${exception.message}"
                    Log.e(TAG, msg)
                    showErrorSnackBar(msg)
                }
        }
    }

    // Method to save event have two possible options:
    // 1. If we're editing and existing event create hash-map and update existing document in db
    // 2. If we're creating a new event pass object to Firestore method to create document
    private fun saveEvent() {
        // get event date from view
        val title: String = binding.eventTitleET.text.toString()
        val description: String = binding.eventDescriptionET.text.toString()
        val owner: String = binding.ownerNameTV.text.toString()
        val date = Timestamp(cal.time)

        // validate form
        if (validateEventForm(title, date)) {
            // make progress bar visible
            binding.createEventPB.visibility = View.VISIBLE
            // 1. edit existing event
            if (eventDetails != null) {
                val eventHashMap = HashMap<String, Any>()
                if (title != eventDetails!!.title) {
                    eventHashMap[Constants.TITLE] = title
                }
                if (description.isNotEmpty() && description != eventDetails!!.description) {
                    eventHashMap[Constants.DESCRIPTION] = description
                }
                if (date != eventDetails!!.date) {
                    eventHashMap[Constants.DATE] = date
                }
                if (eventImageUrl.isNotEmpty() && eventImageUrl != eventDetails!!.eventImgURL) {
                    eventHashMap[Constants.EVENT_IMAGE_URL] = eventImageUrl
                }

                eventHashMap[Constants.TAGS] = eventTagsList

                FirestoreClass().updateEvent(
                    this@CreateEventActivity,
                    eventHashMap,
                    eventDetails!!.documentId
                )
            } else {
                // 2. create event
                val event = Event(
                    title,
                    description,
                    owner,
                    date,
                    eventImageUrl,
                    eventTagsList
                )
                FirestoreClass().createEvent(this@CreateEventActivity, event)
            }
        }
    }

    // Function to call when event upload is successful:
    // hide progress bar and go to main activity
    fun eventUploadSuccess(msg: String) {
        binding.createEventPB.visibility = View.INVISIBLE
        showInfoSnackBar(msg)
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    // Function to call when event upload fails:
    // hide progress bar and show error
    fun eventUploadFail(msg: String) {
        binding.createEventPB.visibility = View.INVISIBLE
        showErrorSnackBar(msg)
    }

    private fun validateEventForm(name: String, date: Timestamp): Boolean {
        return when {
            TextUtils.isEmpty(name) -> {
                showErrorSnackBar(resources.getString(R.string.event_title_no_empty))
                false
            }
            else -> {
                true
            }
        }
    }

    // Method to set user as event owner
    fun setEventOwner(user: User) {
        binding.ownerNameTV.text = user.username
    }

    private fun showAddTagDialog(){
        val dialog = this.createDialog(R.layout.add_tag_dialog, true)
        val button = dialog.findViewById<MaterialButton>(R.id.tagDialogAdd)
        val editText = dialog.findViewById<EditText>(R.id.tagDialogET)
        button.setOnClickListener {
            if (editText.text.toString().isEmpty()) {
                showErrorSnackBar(resources.getString(R.string.enter_text))
            } else {
                val text = editText.text.toString()
                eventTagsList.add(text)
                binding.tagsCG.apply {
                    addChip(text, true) {
                        eventTagsList.forEachIndexed { index, tag ->
                            if (text == tag) {
                                eventTagsList.removeAt(index)
                                binding.tagsCG.removeViewAt(index)
                            }
                        }
                        if (eventTagsList.size == 0){
                            layoutParams.height = 40.dpToPx
                        }
                    }
                    layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                }
                dialog.dismiss()
            }
        }
        dialog.show()
    }
    // Function to show tags as chips in view
    private fun addTags(tagsList: MutableList<String>) {
        if (tagsList.size > 0) {
            binding.tagsCG.apply {
                removeAllViews()
                tagsList.forEachIndexed { index, tag ->
                    addChip(tag, true) {
                        if (isEnabled) {
                            tagsList.removeAt(index)
                            this.removeViewAt(index)
                        }
                    }
                }
            }
        }
    }

}