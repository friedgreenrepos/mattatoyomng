package com.example.mattatoyomng.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mattatoyomng.EventRecyclerAdapter
import com.example.mattatoyomng.activities.CreateEventActivity
import com.example.mattatoyomng.databinding.FragmentEventsBinding
import com.example.mattatoyomng.models.Event
import com.example.mattatoyomng.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore

const val TAG = "EventsFragment"

class EventsFragment : Fragment() {

    private lateinit var binding: FragmentEventsBinding
    private lateinit var eventRecyclerView: RecyclerView
    private lateinit var eventList: ArrayList<Event>
    private lateinit var eventAdapter: EventRecyclerAdapter

    // Firebase references
    private var db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentEventsBinding.inflate(inflater, container, false)

        // initialize recycler view
        eventRecyclerView = binding.eventRV

        // initialize array of events
        // TODO: check if necessary
        eventList = arrayListOf()

        binding.addEventBTN.setOnClickListener() {
            val intent = Intent(this.context, CreateEventActivity::class.java)
            startActivity(intent)
        }

        db.collection(Constants.EVENTS)
            .get()
            .addOnSuccessListener { it ->
                Log.d(TAG, "n.of events in db = ${it.size()}")
                if (!it.isEmpty) {
                    it.forEach {
                        // convert snapshots to Event objects
                        val event = it.toObject(Event::class.java)
                        eventList.add(event)
                    }
                    eventAdapter = EventRecyclerAdapter(eventList)
                    eventRecyclerView.adapter = eventAdapter
                    eventAdapter.notifyDataSetChanged()
                } else {
                    // show "no events" textview if event list is empty
                    binding.noEventsTV.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "ERROR: ${it.message}")
                Toast.makeText(this.context, it.toString(), Toast.LENGTH_SHORT).show()
            }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        eventRecyclerView.setHasFixedSize(true)
        eventRecyclerView.layoutManager = LinearLayoutManager(activity)
    }

}