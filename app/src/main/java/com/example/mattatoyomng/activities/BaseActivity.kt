package com.example.mattatoyomng.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.mattatoyomng.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth


open class BaseActivity : AppCompatActivity() {

    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun getCurrentUserID(): String {
        return FirebaseAuth.getInstance().currentUser!!.uid
    }

    // Function to handle pressing the back button twice
    fun doubleBackToExit() {
        // go back if pressed once
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        // if pressed twice inform user with Toast
        this.doubleBackToExitPressedOnce = true
        Toast.makeText(
            this,
            resources.getString(R.string.please_click_back_again_to_exit),
            Toast.LENGTH_SHORT
        ).show()

        // if user presses back twice within 2s close app
        Handler(Looper.getMainLooper()).postDelayed({
                doubleBackToExitPressedOnce = false
            },
            2000
        )
    }

    fun showErrorSnackBar(message: String) {
        val snackBar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
        val snackBarView = snackBar.view
        snackBarView.setBackgroundColor(
                ContextCompat.getColor(
                        this@BaseActivity,
                        R.color.red_800
                )
        )
        snackBar.show()
    }

    fun showInfoSnackBar(message: String) {
        val snackBar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
        val snackBarView = snackBar.view
        snackBarView.setBackgroundColor(
                ContextCompat.getColor(
                        this@BaseActivity,
                        R.color.green_700
                )
        )
        snackBar.show()
    }

}