package com.example.mattatoyomng.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.mattatoyomng.R
import com.example.mattatoyomng.databinding.ActivityMainBinding
import com.example.mattatoyomng.fragments.EventsFragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val TAG: String = "MainActivity"

    // view binding
    private lateinit var binding: ActivityMainBinding

    // navigation bar toggle
    private lateinit var toggle: ActionBarDrawerToggle

    // drawer layout
    private lateinit var drawerLayout: DrawerLayout

    // firebase auth
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = Firebase.auth

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
                Toast.makeText(this@MainActivity, "profile Item Clicked", Toast.LENGTH_SHORT)
                    .show()
            }
            R.id.logoutItem -> {
                auth.signOut()
                finish()
            }
        }
        return true
    }
}