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

class EventDetailActivity : BaseActivity(), View.OnClickListener,
    FirestoreClass.UpdateEventCallback {

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

    // reminder timestamp global variable
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
                reminderTimestamp
                // update global variable to save to event
                reminderTimestamp = Timestamp(reminderCal.time)
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
            // save event listener
            saveDetailEventBTN.setOnClickListener(this@EventDetailActivity)

            if (eventDetails != null) {
                eventDetailTitleTV.text = eventDetails!!.title
                if (!TextUtils.isEmpty(eventDetails!!.description)) {
                    eventDetailDescriptionTV.text = eventDetails!!.description
                } else {
                    eventDetailDescriptionTV.text = resources.getString(R.string.no_description)
                }
                eventDetailDateTV.text = dateFormatter(eventDetails!!.date, applicationContext)
                eventDetailTimeTV.text = timeFormatter(eventDetails!!.date, applicationContext)

                // show reminders
                val reminderMap = eventDetails!!.userReminderMap
                addReminderInView(reminderMap)

                // show tags
                eventTagsList = eventDetails!!.tags
                addTagsInView(eventTagsList)

                // load event image
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

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.addReminderDetail -> {
                showSaveBtn()
                requestNotificationPermission(v)
            }

            // clicked to remove reminder
            R.id.reminderDetailDate -> {
                deleteReminderInView()
                reminderTimestamp = null
                showSaveBtn()
            }

            R.id.saveDetailEventBTN -> {
                saveEvent()
            }
        }
    }


    private fun saveEvent() {
        // make progress bar visible
        binding.eventDetailPB.visibility = View.VISIBLE
        // define map for user reminder
        val userReminderMap: MutableMap<String, Timestamp> = mutableMapOf()
        // check if reminder has been set:
        // update Event document consequently and only now schedule notification
        if (reminderTimestamp != null) {
            val currentUserID = getCurrentUserID()
            userReminderMap[currentUserID] = reminderTimestamp!!
            scheduleNotification()
        } else {
            deleteNotification()
        }

        val eventHashMap = HashMap<String, Any?>()
        eventHashMap[Constants.REMINDER] = userReminderMap

        FirestoreClass().updateEvent(
            this,
            eventHashMap,
            eventDetails!!.documentId
        )
    }

    private fun showSaveBtn() {
        binding.saveDetailEventBTN.visibility = View.VISIBLE
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
        var reminderTS: Timestamp? = null
        for (key in reminderMap.keys) {
            if (key == getCurrentUserID()) {
                reminderTS = reminderMap[key]
            }
        }
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

    private fun deleteNotification() {
        val intent = Intent(applicationContext, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            notificationID,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    // update view after reminder deletion
    private fun deleteReminderInView() {
        binding.addReminderDetail.visibility = View.VISIBLE
        binding.reminderDetailDate.visibility = View.INVISIBLE
        showInfoSnackBar(resources.getString(R.string.reminder_delete_success))
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
        val message = "Reminder for event: $eventTitle"
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
    }

    // Function to call when event upload is successful:
    // hide progress bar and go to main activity
    private fun eventUpdateSuccess() {
        binding.eventDetailPB.visibility = View.INVISIBLE
        showInfoSnackBar(resources.getString(R.string.update_event_success))
        finish()
    }

    // Function to call when event update fails:
    // hide progress bar and show error
    private fun eventUpdateFail(e: Exception) {
        binding.eventDetailPB.visibility = View.INVISIBLE
        val msg = resources.getString(R.string.update_event_fail) + ": " + e
        showErrorSnackBar(msg)
        finish()
    }

    override fun onUpdateEventSuccess() {
        eventUpdateSuccess()
    }

    override fun onUpdateEventError(e: Exception) {
        eventUpdateFail(e)
    }

}