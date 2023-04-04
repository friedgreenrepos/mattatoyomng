package com.example.mattatoyomng

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mattatoyomng.databinding.FragmentEventsBinding
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class EventsFragment : Fragment() {

    private lateinit var binding: FragmentEventsBinding
    private lateinit var eventRecyclerView: RecyclerView
    private lateinit var eventList: ArrayList<Event>
    private lateinit var eventAdapter: EventRecyclerAdapter

    // Firebase references
    private var db = FirebaseFirestore.getInstance()
    private var storageReference = FirebaseStorage.getInstance().reference

    private var TAG: String = "EventsFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentEventsBinding.inflate(inflater, container, false)
        // initialize recycler view
        eventRecyclerView = binding.eventRV
        // initialize array of events
        eventList = arrayListOf()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        eventRecyclerView.setHasFixedSize(true)
        eventRecyclerView.layoutManager = LinearLayoutManager(activity)
    }

    override fun onStart() {
        super.onStart()

        db.collection("events")
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
    }

    private fun eventChangeListener() {
        db.collection("events")
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        Log.e(TAG, error.message.toString())
                        return
                    }
                    for (dc: DocumentChange in value?.documentChanges!!) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            eventList.add(dc.document.toObject(Event::class.java))
                        }
                    }
                    eventAdapter.notifyDataSetChanged()
                }
            })
    }
}