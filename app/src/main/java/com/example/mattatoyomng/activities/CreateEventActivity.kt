package com.example.mattatoyomng.activities

import android.app.*
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.example.mattatoyomng.R
import com.example.mattatoyomng.databinding.ActivityCreateEventBinding
import com.example.mattatoyomng.firebase.FirestoreClass
import com.example.mattatoyomng.fragments.EventsFragment
import com.example.mattatoyomng.models.*
import com.example.mattatoyomng.utils.*
import com.google.android.material.button.MaterialButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException
import java.util.*


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
    private var eventCal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private lateinit var timeSetListener: TimePickerDialog.OnTimeSetListener
    private var hasEditedDate = false
    private var hasEditedTime = false

    // calendar for reminder
    private var reminderCal = Calendar.getInstance()
    private lateinit var reminderDateSetListener: DatePickerDialog.OnDateSetListener
    private lateinit var reminderTimeSetListener: TimePickerDialog.OnTimeSetListener

    // reminder timestamp
    private var reminderTimestamp: Timestamp? = null

    // event passed in intent
    private var eventDetails: Event? = null

    // tag list
    private var eventTagsList: MutableList<String> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // initialize date picker listener
        dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            eventCal.set(Calendar.YEAR, year)
            eventCal.set(Calendar.MONTH, month)
            eventCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            hasEditedDate = true
            updateDateInView()
        }

        // initialize time picker listener
        timeSetListener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            eventCal.set(Calendar.HOUR_OF_DAY, hourOfDay)
            eventCal.set(Calendar.MINUTE, minute)
            hasEditedTime = true
            updateTimeInView()
        }

        // initialize reminder date picker listener
        reminderDateSetListener =
            DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                reminderCal.set(Calendar.YEAR, year)
                reminderCal.set(Calendar.MONTH, month)
                reminderCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                showReminderTimePicker()
            }

        // initialize time picker listener
        reminderTimeSetListener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            reminderCal.set(Calendar.HOUR_OF_DAY, hourOfDay)
            reminderCal.set(Calendar.MINUTE, minute)
            updateReminderInView()
            // update global variable to save to event
            reminderTimestamp = Timestamp(reminderCal.time)
        }

        // check if event is passed in intent, if so we're in edit mode
        if (intent.hasExtra(EventsFragment.EVENT_DETAILS)) {
            eventDetails =
                intent.getParcelableExtra(EventsFragment.EVENT_DETAILS, Event::class.java)
        }

        // create notification channel
        createNotificationChannel()

        // set-up layout:
        // if eventDetails exists -> create event
        // else -> edit event
        binding.apply {
            toolbar = toolbarCreateEvent
            eventDateTV.setOnClickListener(this@CreateEventActivity)
            eventTimeTV.setOnClickListener(this@CreateEventActivity)
            // upload image listener
            addEventImageTV.setOnClickListener(this@CreateEventActivity)
            // save/update event listener
            saveEventBTN.setOnClickListener(this@CreateEventActivity)
            // add tags listener
            addTagLL.setOnClickListener(this@CreateEventActivity)
            // set reminder listener
            addReminderTV.setOnClickListener(this@CreateEventActivity)
            // delete reminder listener
            reminderDateTV.setOnClickListener(this@CreateEventActivity)

            // load event data if event details is passed in intent
            if (eventDetails != null) {
                setupActionBarEdit()
                eventTitleET.setText(eventDetails!!.title)
                eventDescriptionET.setText(eventDetails!!.description)
                eventDateTV.text = dateFormatter(eventDetails!!.date, applicationContext)
                eventTimeTV.text = timeFormatter(eventDetails!!.date, applicationContext)
                ownerNameTV.text = eventDetails!!.owner
                eventTagsList = eventDetails!!.tags
                addTagsInView(eventTagsList)
                val reminderMap = eventDetails!!.userReminderMap
                addReminderInView(reminderMap)
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

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.eventDateTV -> {
                DatePickerDialog(
                    this@CreateEventActivity,
                    dateSetListener,
                    eventCal.get(Calendar.YEAR),
                    eventCal.get(Calendar.MONTH),
                    eventCal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
            R.id.eventTimeTV -> {
                TimePickerDialog(
                    this@CreateEventActivity,
                    timeSetListener,
                    eventCal.get(Calendar.HOUR_OF_DAY),
                    eventCal.get(Calendar.MINUTE),
                    DateFormat.is24HourFormat(this@CreateEventActivity)
                ).show()
            }
            R.id.addEventImageTV -> {
                requestStoragePermission(v)
            }
            R.id.saveEventBTN -> {
                if (eventImageUri != null) uploadEventImage()
                else saveEvent()
            }
            R.id.addTagLL -> {
                showAddTagDialog()
            }
            R.id.addReminderTV -> {
                requestNotificationPermission(v)
            }
            R.id.reminderDateTV -> {
                deleteReminder()
            }
        }
    }

    private fun createNotificationChannel() {
        val name = "Notification Channel"
        val desc = "A Description"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelID, name, importance)
        channel.description = desc
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun scheduleNotification() {
        val intent = Intent(applicationContext, NotificationReceiver::class.java)
        val title = resources.getString(R.string.app_name)
        val eventTitle = binding.eventTitleET.text.toString()
        var message = "Reminder for event"
        if (!TextUtils.isEmpty(eventTitle)) {
            message += ": $eventTitle"
        }
        intent.putExtra(titleExtra, title)
        intent.putExtra(messageExtra, message)

        // create pending intent
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            notificationID,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // setup and call alarm manager, get notification time from reminder calendar
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val time = reminderCal.timeInMillis
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            time,
            pendingIntent
        )
        showAlert(time, title, message)
    }

    private fun deleteReminder() {
        val intent = Intent(applicationContext, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            notificationID,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // cancel notification by passing same intent of its creation
        alarmManager.cancel(pendingIntent)
        // reset reminder global variable
        reminderTimestamp = null
        // update layout
        deleteReminderInView()
        showInfoSnackBar(resources.getString(R.string.reminder_delete_success))
    }

    // update view after reminder deletion
    private fun deleteReminderInView() {
        binding.addReminderTV.visibility = View.VISIBLE
        binding.reminderDateTV.visibility = View.INVISIBLE
    }

    private fun updateDateInView() {
        binding.eventDateTV.text = dateFormatter(Timestamp(eventCal.time), applicationContext)
    }

    private fun updateTimeInView() {
        binding.eventTimeTV.text = timeFormatter(Timestamp(eventCal.time), applicationContext)
    }

    override fun showReminderDatePicker() {
        DatePickerDialog(
            this@CreateEventActivity,
            reminderDateSetListener,
            reminderCal.get(Calendar.YEAR),
            reminderCal.get(Calendar.MONTH),
            reminderCal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // show time picker
    private fun showReminderTimePicker() {
        TimePickerDialog(
            this@CreateEventActivity,
            reminderTimeSetListener,
            reminderCal.get(Calendar.HOUR_OF_DAY),
            reminderCal.get(Calendar.MINUTE),
            DateFormat.is24HourFormat(this@CreateEventActivity)
        ).show()
    }

    // update reminder date time in view
    private fun updateReminderInView() {
        val reminderTs = Timestamp(reminderCal.time)
        val reminderString = dateFormatter(reminderTs, applicationContext) +
                " " + timeFormatter(reminderTs, applicationContext)
        binding.addReminderTV.visibility = View.INVISIBLE
        binding.reminderDateTV.visibility = View.VISIBLE
        binding.reminderDateTV.text = reminderString
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
    override var openGalleryLauncher: ActivityResultLauncher<Intent> =
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
                // change "add image" button text
                addEventImageTV.setText(R.string.update_event_image)
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
        val date = Timestamp(eventCal.time)

        // validate form
        if (validateEventForm(title, date)) {
            // make progress bar visible
            binding.createEventPB.visibility = View.VISIBLE

            // define map for user reminder
            val userReminderMap: MutableMap<String, Timestamp> = mutableMapOf()
            // check if reminder has been set:
            // update Event document consequently and only now schedule notification
            if (reminderTimestamp != null) {
                val currentUserID = getCurrentUserID()
                userReminderMap[currentUserID] = reminderTimestamp!!
                scheduleNotification()
            }

            // 1. edit existing event
            if (eventDetails != null) {
                val eventHashMap = HashMap<String, Any?>()
                if (title != eventDetails!!.title) {
                    eventHashMap[Constants.TITLE] = title
                    eventHashMap[Constants.KEYWORDS] = title.lowercase()
                }
                if (description.isNotEmpty() && description != eventDetails!!.description) {
                    eventHashMap[Constants.DESCRIPTION] = description
                }
                if ((hasEditedDate || hasEditedTime) && date != eventDetails!!.date) {
                    eventHashMap[Constants.DATE] = date
                }
                if (eventImageUrl.isNotEmpty() && eventImageUrl != eventDetails!!.eventImgURL) {
                    eventHashMap[Constants.EVENT_IMAGE_URL] = eventImageUrl
                }

                eventHashMap[Constants.REMINDER] = userReminderMap

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
                    eventTagsList,
                    title.lowercase(),
                    userReminderMap,
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
        // this way we can't go back to create event >:)
        finish()
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

    // show dialog to add tags as chips in chipgroup
    private fun showAddTagDialog() {
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
                        if (eventTagsList.size == 0) {
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
    private fun addTagsInView(tagsList: MutableList<String>) {
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

    private fun addReminderInView(reminderMap: Map<String, Timestamp?>) {
        var reminderTS: Timestamp? = null
        for (key in reminderMap.keys) {
            if (key == getCurrentUserID()) {
                reminderTS = reminderMap[key]
            }
        }
        if (reminderTS != null) {
            val reminderString = dateFormatter(reminderTS, applicationContext) +
                    " " + timeFormatter(reminderTS, applicationContext)
            binding.addReminderTV.visibility = View.INVISIBLE
            binding.reminderDateTV.visibility = View.VISIBLE
            binding.reminderDateTV.text = reminderString
        }
    }

}