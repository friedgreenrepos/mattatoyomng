package com.example.mattatoyomng.activities

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.example.mattatoyomng.R
import com.example.mattatoyomng.databinding.ActivityEventDetailBinding
import com.example.mattatoyomng.fragments.EventsFragment
import com.example.mattatoyomng.models.Event
import com.example.mattatoyomng.utils.addChip
import com.example.mattatoyomng.utils.addChipNoClose
//import com.example.mattatoyomng.utils.addChipNoClose
import com.example.mattatoyomng.utils.dateFormatter
import com.example.mattatoyomng.utils.timeFormatter
import java.io.IOException

class EventDetailActivity : AppCompatActivity() {

    private val TAG: String = "CreateEventActivity"

    // binding
    private lateinit var binding: ActivityEventDetailBinding

    // Toolbar
    private lateinit var toolbar: Toolbar

    // event passed in intent
    private var eventDetails: Event? = null

    // event tags list
    private var eventTagsList: MutableList<String> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // check if event is passed in intent, if so we're in edit mode
        if (intent.hasExtra(EventsFragment.EVENT_DETAILS)) {
            eventDetails =
                intent.getParcelableExtra(EventsFragment.EVENT_DETAILS, Event::class.java)
        }

        binding.apply {
            // toolbar setup
            toolbar = toolbarEventDetail
            setupActionBar()

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
}