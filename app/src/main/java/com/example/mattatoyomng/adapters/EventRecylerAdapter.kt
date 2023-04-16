package com.example.mattatoyomng.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mattatoyomng.R
import com.example.mattatoyomng.activities.EventCreateUpdateActivity
import com.example.mattatoyomng.databinding.ItemEventBinding
import com.example.mattatoyomng.firebase.FirestoreClass
import com.example.mattatoyomng.fragments.EventsFragment
import com.example.mattatoyomng.models.Event
import com.example.mattatoyomng.utils.addChip
import com.example.mattatoyomng.utils.dateFormatter
import com.example.mattatoyomng.utils.hide
import com.example.mattatoyomng.utils.timeFormatter
import java.io.IOException

class EventRecyclerAdapter(
    private val context: Context,
    private val eventList: MutableList<Event>
) :
    RecyclerView.Adapter<EventRecyclerAdapter.EventViewHolder>(),
    FirestoreClass.DeleteEventCallback {

    private var TAG = "EventRecyclerAdapter"

    lateinit var binding: ItemEventBinding
    private var onClickListener: OnClickListener? = null

    inner class EventViewHolder(binding: ItemEventBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event) {
            binding.event = event

            // load event image
            try {
                Glide
                    .with(context)
                    .load(event.eventImgURL)
                    .centerCrop()
                    .placeholder(R.drawable.image_red_transparency4)
                    .into(binding.eventImageIV)
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "Error loading profile pic: ${e.message}")
            }

            // format date and time
            binding.cardEventDateTV.text = dateFormatter(event.date, context)
            binding.cardEventTimeTV.text = timeFormatter(event.date, context)

            // add ellipsis to description
            binding.eventDescriptionTV.apply {
                text = if (event.description.length > 120) {
                    "${event.description.substring(0, 120)}..."
                } else {
                    event.description
                }
            }

            // add tags
            binding.eventItemTagsCG.apply {
                if (event.tags.isEmpty()) {
                    hide()
                } else {
                    removeAllViews()
                    event.tags.forEach { tag -> addChip(tag) }
                }
            }

            // click on item
            itemView.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick(position, event)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        binding = ItemEventBinding.inflate(
            LayoutInflater.from(context),
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

    interface OnClickListener {
        fun onClick(position: Int, model: Event)
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    fun notifyEditItem(fragment: Fragment, position: Int, requestCode: Int) {
        val intent = Intent(context, EventCreateUpdateActivity::class.java)
        intent.putExtra(EventsFragment.EVENT_DETAILS, eventList[position])
        fragment.startActivityForResult(intent, requestCode)
        notifyItemChanged(position)
    }

    fun removeAt(position: Int) {
        val documentId = eventList[position].documentId
        FirestoreClass().deleteEvent(this, documentId, position)
    }

    override fun onDeleteEventSuccess(position: Int) {
        eventList.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun onDeleteEventFail(e: Exception) {
        // TODO: avoid creating fragment
        Log.d(TAG, "EVENT DELETE FAIL: $e")
        val fragment = EventsFragment()
        fragment.showErrorSnackBar(fragment.resources.getString(R.string.delete_event_fail))
    }
}