package com.example.mattatoyomng.activities

import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.example.mattatoyomng.R
import com.example.mattatoyomng.databinding.ActivityCreateEventBinding
import com.example.mattatoyomng.databinding.ActivityEditEventBinding
import com.example.mattatoyomng.fragments.EventsFragment
import com.example.mattatoyomng.models.Event
import com.example.mattatoyomng.utils.dateFormatter
import com.example.mattatoyomng.utils.timeFormatter
import java.io.IOException

class EditEventActivity : AppCompatActivity() {
    private val TAG: String = "EditEventActivity"

    // binding
    private lateinit var binding: ActivityEditEventBinding

    // Toolbar
    private lateinit var toolbar: Toolbar

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var eventDetails: Event? = null
        if (intent.hasExtra(EventsFragment.EVENT_DETAIL)) {
            eventDetails = intent.getParcelableExtra(EventsFragment.EVENT_DETAIL, Event::class.java)
        }
        if (eventDetails != null) {
            binding.editEventTitleET.setText(eventDetails.title)
            binding.editEventDescriptionET.setText(eventDetails.description)
            binding.editEventDateTV.text = dateFormatter(eventDetails.date)
            binding.editEventTimeTV.text = timeFormatter(eventDetails.date)
            binding.ownerNameTV.text = eventDetails.owner

            try {
                Glide
                    .with(this@EditEventActivity)
                    .load(eventDetails.eventImgURL)
                    .centerCrop()
                    .placeholder(R.drawable.image_red_transparency4)
                    .into(binding.eventImageEditIV)
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "Error loading event image: ${e.message}")
            }

        }


        binding.apply {
            // toolbar setup
            toolbar = toolbarEditEvent
            setupActionBar()
        }
    }

    private fun setupActionBar() {
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
}