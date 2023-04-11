package com.example.mattatoyomng.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.mattatoyomng.R
import com.example.mattatoyomng.databinding.ActivityMainBinding
import com.example.mattatoyomng.firebase.FirestoreClass
import com.example.mattatoyomng.fragments.EventsFragment
import com.example.mattatoyomng.fragments.UpdateProfileFragment
import com.example.mattatoyomng.models.User
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth


class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val TAG: String = "MainActivity"

    // view binding
    private lateinit var binding: ActivityMainBinding

    // navigation bar toggle
    private lateinit var toggle: ActionBarDrawerToggle

    // drawer layout
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Navigation drawer setup
        drawerLayout = binding.drawerLayout
        toggle = ActionBarDrawerToggle(
            this@MainActivity, drawerLayout,
            R.string.open, R.string.close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val navView = binding.navView
        navView.setNavigationItemSelectedListener(this)

        // set default fragment
        if (savedInstanceState == null) {
            // TODO: swap string 'Events' for menu item title. How to access title from item?
            replaceFragment(EventsFragment(), "Events")
            navView.setCheckedItem((R.id.eventsItem))
        }

        // load user info
        FirestoreClass().loadUserData(this@MainActivity)
    }

    private fun replaceFragment(fragment: Fragment, title: String) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()
        drawerLayout.closeDrawers()
        setTitle(title)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        item.isChecked = true
        when (item.itemId) {
            R.id.eventsItem -> {
                replaceFragment(EventsFragment(), item.title.toString())
            }
            R.id.searchItem -> {
                Toast.makeText(this@MainActivity, "search Item Clicked", Toast.LENGTH_SHORT)
                    .show()
            }
            R.id.todoItem -> {
                Toast.makeText(this@MainActivity, "todo Item Clicked", Toast.LENGTH_SHORT)
                    .show()
            }
            R.id.profileItem -> {
                replaceFragment(UpdateProfileFragment(), item.title.toString())
//                val intent = Intent(this@MainActivity, UpdateProfileActivity::class.java)
//                startActivity(intent)
            }
            R.id.logoutItem -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this@MainActivity, IntroActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        return true
    }

    // Function to update the navigation bar with user detail.
    fun updateNavigationUserDetails(user: User) {
        val headerView = binding.navView.getHeaderView(0)
        // Set user profile picture in nav header
        val navUserProfilePic = headerView.findViewById<ImageView>(R.id.navUserProfilePic)
        Glide
            .with(this@MainActivity)
            .load(user.profilePic)
            .centerCrop()
            .placeholder(R.drawable.user_white_80)
            .into(navUserProfilePic)

        // Set user username in nav header
        val navUsername = headerView.findViewById<TextView>(R.id.usernameTV)
        navUsername.text = user.username
    }
}