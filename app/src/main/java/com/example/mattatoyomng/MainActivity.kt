package com.example.mattatoyomng

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.mattatoyomng.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var binding: ActivityMainBinding

    //    navigation bar toggle
    lateinit var toggle: ActionBarDrawerToggle

    lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CalendarFragment()).commit()
            navView.setCheckedItem((R.id.calendarItem))
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
            R.id.calendarItem -> {
                replaceFragment(CalendarFragment(), item.title.toString())
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
        }
        return true
    }
}