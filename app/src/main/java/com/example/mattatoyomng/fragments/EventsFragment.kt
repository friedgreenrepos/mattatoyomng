package com.example.mattatoyomng.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mattatoyomng.EventRecyclerAdapter
import com.example.mattatoyomng.activities.CreateEventActivity
import com.example.mattatoyomng.databinding.FragmentEventsBinding
import com.example.mattatoyomng.firebase.FirestoreClass
import com.example.mattatoyomng.models.Event
import com.example.mattatoyomng.utils.SwipeToEditCallback
import com.google.firebase.firestore.FirebaseFirestore

const val TAG = "EventsFragment"

class EventsFragment : BaseFragment() {

    private lateinit var binding: FragmentEventsBinding
    private lateinit var eventRecyclerView: RecyclerView
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

        binding.addEventBTN.setOnClickListener() {
            val intent = Intent(this.context, CreateEventActivity::class.java)
            startActivity(intent)
        }

        // initialize recycler view
        eventRecyclerView = binding.eventRV

        FirestoreClass().getEventsList(this@EventsFragment)

        return binding.root
    }

    fun setupEventsRecyclerView(eventList: ArrayList<Event>) {
        if (eventList.size == 0) {
            // show "no events" textview if event list is empty
            binding.noEventsTV.visibility = View.VISIBLE
        } else {
            // setup event adapter using event list from firestore
            eventAdapter = EventRecyclerAdapter(requireContext(), eventList)
            eventRecyclerView.adapter = eventAdapter
            eventAdapter.notifyDataSetChanged()
            val editSwipeHandler = object : SwipeToEditCallback(this.requireContext()) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    eventAdapter.notifyEditItem(
                        this@EventsFragment,
                        viewHolder.absoluteAdapterPosition,
                        EDIT_EVENT_REQUEST_CODE
                    )
                }
            }
            val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
            editItemTouchHelper.attachToRecyclerView(eventRecyclerView)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        eventRecyclerView.setHasFixedSize(true)
        eventRecyclerView.layoutManager = LinearLayoutManager(activity)
    }

    companion object {
        internal const val EVENT_DETAILS = "event_details"
        private const val EDIT_EVENT_REQUEST_CODE = 1
    }

}