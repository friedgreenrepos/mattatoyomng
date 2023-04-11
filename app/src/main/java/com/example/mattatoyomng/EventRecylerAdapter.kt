package com.example.mattatoyomng

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mattatoyomng.databinding.EventCardBinding
import com.example.mattatoyomng.models.Event
import java.text.SimpleDateFormat
import java.util.*

class EventRecyclerAdapter(private val eventList: ArrayList<Event>) :
    RecyclerView.Adapter<EventRecyclerAdapter.EventViewHolder>() {

    lateinit var binding: EventCardBinding
    inner class EventViewHolder(binding: EventCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event) {
            binding.event = event
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        binding = EventCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EventViewHolder(binding)
    }

    override fun getItemCount(): Int = eventList.size

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event: Event = eventList[position]
        holder.bind(event)
        val dateFormat = "dd MMMM yyyy"
        val timeFormat = "hh:mm"
        val sdfDate = SimpleDateFormat(dateFormat, Locale.getDefault())
        val sdfTime = SimpleDateFormat(timeFormat, Locale.getDefault())
        binding.cardEventDateTV.text = sdfDate.format(event.date!!.toDate()).toString()
        binding.cardEventTimeTV.text = sdfTime.format(event.date.toDate()).toString()
    }
}