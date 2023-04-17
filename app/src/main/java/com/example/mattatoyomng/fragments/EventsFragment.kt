package com.example.mattatoyomng.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mattatoyomng.R
import com.example.mattatoyomng.activities.EventCreateUpdateActivity
import com.example.mattatoyomng.activities.EventDetailActivity
import com.example.mattatoyomng.adapters.EventRecyclerAdapter
import com.example.mattatoyomng.databinding.FragmentEventsBinding
import com.example.mattatoyomng.firebase.FirestoreClass
import com.example.mattatoyomng.models.Event
import com.example.mattatoyomng.models.User
import com.example.mattatoyomng.utils.SwipeToDeleteCallback
import com.example.mattatoyomng.utils.SwipeToEditCallback
import com.example.mattatoyomng.utils.getTodayTimestamp

class EventsFragment : BaseFragment(), FirestoreClass.GetEventListCallback,
    FirestoreClass.GetUserDataCallback {

    private val TAG = "EventsFragment"

    private lateinit var binding: FragmentEventsBinding
    private var eventRecyclerView: RecyclerView? = null
    private var eventAdapter: EventRecyclerAdapter? = null

    private var isUserAdmin: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentEventsBinding.inflate(inflater, container, false)

        // initialize recycler view
        eventRecyclerView = binding.eventRV

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // check if user is admin
        FirestoreClass().getUserData(this)

        // setup search view in menu
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.event_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                val searchView = menuItem.actionView as SearchView
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
                    FirestoreClass.GetEventListCallback, FirestoreClass.GetEventListSearchCallback {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        val searchText = query!!.lowercase()
                        if (searchText.isNotEmpty()) {
                            FirestoreClass().searchInFirestore(this, searchText)
                        } else {
                            FirestoreClass().getEventsList(this)
                        }
                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        val searchText = newText!!.lowercase()
                        if (searchText.isNotEmpty()) {
                            FirestoreClass().searchInFirestore(this, searchText)
                        } else {
                            FirestoreClass().getEventsList(this)
                        }
                        return false
                    }

                    override fun onGetEventListResult(eventList: MutableList<Event>) {
                        setupEventsRecyclerView(eventList)
                    }

                    override fun onGetEventListSearch(eventList: MutableList<Event>) {
                        eventList.removeAll { it.date!! < getTodayTimestamp() }
                        setupEventsRecyclerView(eventList)
                    }
                })
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }


    override fun onResume() {
        super.onResume()
        // get event list from db
        FirestoreClass().getEventsList(this)
    }

    private fun setupEventsRecyclerView(eventList: MutableList<Event>) {
        // setup event adapter using event list from firestore
        eventRecyclerView?.setHasFixedSize(true)
        eventRecyclerView?.layoutManager = LinearLayoutManager(activity)
        eventAdapter = EventRecyclerAdapter(this.requireContext(), eventList)
        eventRecyclerView?.adapter = eventAdapter
        eventAdapter!!.notifyDataSetChanged()

        // click on item to see detail activity
        eventAdapter!!.setOnClickListener(object : EventRecyclerAdapter.OnClickListener {
            override fun onClick(position: Int, model: Event) {
                val intent = Intent(requireContext(), EventDetailActivity::class.java)
                intent.putExtra(EVENT_DETAILS, model)
                startActivity(intent)
            }
        })

        if (isUserAdmin) {
            // setup swipe left to edit
            val editSwipeHandler = object : SwipeToEditCallback(this.requireContext()) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    eventAdapter!!.notifyEditItem(
                        this@EventsFragment,
                        viewHolder.absoluteAdapterPosition,
                        EDIT_EVENT_REQUEST_CODE
                    )
                }
            }
            val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
            editItemTouchHelper.attachToRecyclerView(eventRecyclerView)

            // setup swipe right to delete
            val deleteSwipeHandler = object : SwipeToDeleteCallback(this.requireContext()) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    eventAdapter!!.removeAt(viewHolder.absoluteAdapterPosition)
                }
            }
            val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
            deleteItemTouchHelper.attachToRecyclerView(eventRecyclerView)
        }

    }

    override fun onGetEventListResult(eventList: MutableList<Event>) {
        setupEventsRecyclerView(eventList)
    }

    companion object {
        internal const val EVENT_DETAILS = "event_details"
        private const val EDIT_EVENT_REQUEST_CODE = 1
    }

    private fun updateAdminUI(user: User) {
        if (user.admin) {
            binding.addEventBTN.visibility = View.VISIBLE
            binding.addEventBTN.setOnClickListener {
                val intent = Intent(this.context, EventCreateUpdateActivity::class.java)
                startActivity(intent)
            }
            // setting global variable
            isUserAdmin = true
        } else {
            binding.addEventBTN.visibility = View.INVISIBLE
            // setting global variable
            isUserAdmin = false
        }
    }

    override fun onGetUserDataSuccess(user: User) {
        updateAdminUI(user)
    }

    override fun onGetUserDataFail(e: Exception) {
        Log.d(TAG, e.message!!)
    }
}