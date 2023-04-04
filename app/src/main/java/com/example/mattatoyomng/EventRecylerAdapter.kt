package com.example.mattatoyomng

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mattatoyomng.databinding.EventCardBinding

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
    }
}