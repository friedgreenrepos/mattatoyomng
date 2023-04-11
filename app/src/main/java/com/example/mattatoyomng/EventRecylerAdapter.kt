package com.example.mattatoyomng

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mattatoyomng.databinding.EventCardBinding
import com.example.mattatoyomng.models.Event
import com.example.mattatoyomng.utils.dateFormatter
import com.example.mattatoyomng.utils.timeFormatter
import java.text.SimpleDateFormat
import java.util.*

class EventRecyclerAdapter(private val eventList: ArrayList<Event>) :
    RecyclerView.Adapter<EventRecyclerAdapter.EventViewHolder>() {

    lateinit var binding: EventCardBinding
    private var onClickListener: OnClickListener? = null
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

        // format date and time
        binding.cardEventDateTV.text = dateFormatter(event.date)
        binding.cardEventTimeTV.text = timeFormatter(event.date)

        // bind edit event
        binding.editEventIV.setOnClickListener {
            if (onClickListener != null) {
                onClickListener!!.onClick(position, event)
            }
        }
    }

    interface OnClickListener{
        fun onClick(position: Int, model: Event)
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }
}