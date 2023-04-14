package com.example.mattatoyomng.activities

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.example.mattatoyomng.R
import com.example.mattatoyomng.databinding.ActivityEventDetailBinding
import com.example.mattatoyomng.firebase.FirestoreClass
import com.example.mattatoyomng.fragments.EventsFragment
import com.example.mattatoyomng.models.*
import com.example.mattatoyomng.utils.Constants
import com.example.mattatoyomng.utils.addChipNoClose
import com.example.mattatoyomng.utils.dateFormatter
import com.example.mattatoyomng.utils.timeFormatter
import com.google.firebase.Timestamp
import java.io.IOException
import java.util.*

class EventDetailActivity : BaseActivity(), View.OnClickListener {

    val TAG: String = "EventDetailActivity"

    // binding
    private lateinit var binding: ActivityEventDetailBinding

    // Toolbar
    private lateinit var toolbar: Toolbar

    // event passed in intent
    private var eventDetails: Event? = null

    // event tags list
    private var eventTagsList: MutableList<String> = arrayListOf()

    // calendar for reminder
    private var reminderCal = Calendar.getInstance()
    private lateinit var reminderDateSetListener: DatePickerDialog.OnDateSetListener
    private lateinit var reminderTimeSetListener: TimePickerDialog.OnTimeSetListener

    // reminder timestamp
    private var reminderTimestamp: Timestamp? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // check if event is passed in intent, if so we're in edit mode
        if (intent.hasExtra(EventsFragment.EVENT_DETAILS)) {
            eventDetails =
                intent.getParcelableExtra(EventsFragment.EVENT_DETAILS, Event::class.java)
        }

        // create notification channel
        createNotificationChannel()

        // initialize reminder date picker listener
        reminderDateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                reminderCal.set(Calendar.YEAR, year)
                reminderCal.set(Calendar.MONTH, month)
                reminderCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                showReminderTimePicker()
            }

        // initialize time picker listener
        reminderTimeSetListener =
            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                reminderCal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                reminderCal.set(Calendar.MINUTE, minute)
                scheduleNotification()
                updateReminderInView()
            }

        binding.apply {
            // toolbar setup
            toolbar = toolbarEventDetail
            setupActionBar()

            // set reminder listener
            addReminderDetail.setOnClickListener(this@EventDetailActivity)
            // delete reminder listener
            reminderDetailDate.setOnClickListener(this@EventDetailActivity)

            if (eventDetails != null) {
                eventDetailTitleTV.text = eventDetails!!.title
                if (!TextUtils.isEmpty(eventDetails!!.description)) {
                    eventDetailDescriptionTV.text = eventDetails!!.description
                } else {
                    eventDetailDescriptionTV.text = resources.getString(R.string.no_description)
                }
                eventDetailDateTV.text = dateFormatter(eventDetails!!.date, applicationContext)
                eventDetailTimeTV.text = timeFormatter(eventDetails!!.date, applicationContext)

                // tags setup
                eventTagsList = eventDetails!!.tags
                addTagsInView(eventTagsList)
                try {
                    Glide
                        .with(this@EventDetailActivity)
                        .load(eventDetails!!.eventImgURL)
                        .centerCrop()
                        .placeholder(R.drawable.image_red_transparency4)
                        .into(eventDetailImageIV)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.e(TAG, "Error loading event image: ${e.message}")
                }
            }
        }
    }

    override fun onResume() {
        // check if event is passed in intent, if so we're in edit mode
        if (intent.hasExtra(EventsFragment.EVENT_DETAILS)) {
            eventDetails =
                intent.getParcelableExtra(EventsFragment.EVENT_DETAILS, Event::class.java)
        }
        Log.d(TAG, "on resume...")
        Log.d(TAG, "eventdetails = $eventDetails")
        val reminderMap = eventDetails!!.userReminderMap
        addReminderInView(reminderMap)
        super.onResume()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.addReminderDetail -> {
                requestNotificationPermission(v)
            }
            R.id.reminderDetailDate -> {
                deleteReminder()
            }
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar

        // add back button and back navigation functionality and remove title
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.arrow_back_white)
            actionBar.title = "Event details"
        }
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    // Function to show tags as chips in view
    private fun addTagsInView(tagsList: MutableList<String>) {
        if (tagsList.size > 0) {
            binding.tagsDetailCG.apply {
                removeAllViews()
                tagsList.forEachIndexed { index, tag ->
                    addChipNoClose(tag, true) {
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
        Log.d(TAG, "addReminderInView...")
        var reminderTS: Timestamp? = null
        for (key in reminderMap.keys) {
            if (key == getCurrentUserID()) {
                reminderTS = reminderMap[key]
            }
        }
        Log.d(TAG, "reminderTS = $reminderTS")
        if (reminderTS != null) {
            val reminderString = dateFormatter(reminderTS, applicationContext) +
                    " " + timeFormatter(reminderTS, applicationContext)
            binding.addReminderDetail.visibility = View.INVISIBLE
            binding.reminderDetailDate.visibility = View.VISIBLE
            binding.reminderDetailDate.text = reminderString
        } else {
            binding.addReminderDetail.visibility = View.VISIBLE
            binding.reminderDetailDate.visibility = View.INVISIBLE
        }
    }

    override fun showReminderDatePicker() {
        DatePickerDialog(
            this@EventDetailActivity,
            reminderDateSetListener,
            reminderCal.get(Calendar.YEAR),
            reminderCal.get(Calendar.MONTH),
            reminderCal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // show time picker
    private fun showReminderTimePicker() {
        TimePickerDialog(
            this@EventDetailActivity,
            reminderTimeSetListener,
            reminderCal.get(Calendar.HOUR_OF_DAY),
            reminderCal.get(Calendar.MINUTE),
            DateFormat.is24HourFormat(this@EventDetailActivity)
        ).show()
    }

    // update reminder date time in view
    private fun updateReminderInView() {
        val reminderTs = Timestamp(reminderCal.time)
        val reminderString = dateFormatter(reminderTs, applicationContext) +
                " " + timeFormatter(reminderTs, applicationContext)
        binding.addReminderDetail.visibility = View.INVISIBLE
        binding.reminderDetailDate.visibility = View.VISIBLE
        binding.reminderDetailDate.text = reminderString
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
        alarmManager.cancel(pendingIntent)
        deleteReminderInView()
        // update global var containing reminder timestamp
        reminderTimestamp = null
        // update event document with reminder
        val eventHashMap = HashMap<String, Any?>()
        val userReminderMap: MutableMap<String, Timestamp> = mutableMapOf()
        eventHashMap[Constants.REMINDER] = userReminderMap
        FirestoreClass().updateEvent(
            this@EventDetailActivity,
            eventHashMap,
            eventDetails!!.documentId
        )
        showInfoSnackBar("Reminder successfully deleted")
    }

    // update view after reminder deletion
    private fun deleteReminderInView() {
        binding.addReminderDetail.visibility = View.VISIBLE
        binding.reminderDetailDate.visibility = View.INVISIBLE
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
        val eventTitle = eventDetails!!.title
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
        // update global variable to save to event
        reminderTimestamp = Timestamp(reminderCal.time)
        // update event document with reminder
        val eventHashMap = HashMap<String, Any?>()
        val userReminderMap: MutableMap<String, Timestamp> = mutableMapOf()
        val currentUserID = getCurrentUserID()
        userReminderMap[currentUserID] = reminderTimestamp!!
        eventHashMap[Constants.REMINDER] = userReminderMap
        FirestoreClass().updateEvent(
            this@EventDetailActivity,
            eventHashMap,
            eventDetails!!.documentId
        )
        showAlert(time, title, message)
    }

}