package com.example.mattatoyomng.activities

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.format.DateFormat
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mattatoyomng.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import java.util.*


open class BaseActivity : AppCompatActivity() {

    private var doubleBackToExitPressedOnce = false

    open lateinit var openGalleryLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun getCurrentUserID(): String {
        return FirebaseAuth.getInstance().currentUser!!.uid
    }

    open fun showReminderDatePicker() {}

    // Method to request storage permission
    open fun requestStoragePermission(view: View) {
        // Check if the permission was denied and show rationale
        if (
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            )
        ) {
            showErrorSnackBar(getString(R.string.permission_storage_required))
        } else {
            // If it has not been denied then request, directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            requestPermissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES
                )
            )
        }
    }

    // Method to request notification permission
    open fun requestNotificationPermission(view: View) {
        // Check if the permission was denied and show rationale
        if (
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            )
        ) {
            showErrorSnackBar(getString(R.string.permission_notification_required))
        } else {
            // If it has not been denied then request, directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            requestPermissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            )
        }
    }

    // ActivityResultLauncher for MultiplePermissions
    private val requestPermissionsLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                val permissionName = it.key
                val isGranted = it.value
                // if permission is granted perform operation
                if (isGranted) {
                    // If permission name is READ_MEDIA_IMAGES, call gallery launcher with pick intent
                    if (permissionName == Manifest.permission.READ_MEDIA_IMAGES) {
                        val pickIntent =
                            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        openGalleryLauncher.launch(pickIntent)
                    }
                    // If permission name is POST_NOTIFICATIONS schedule notification
                    if (permissionName == Manifest.permission.POST_NOTIFICATIONS) {
                        showReminderDatePicker()
                    }

                } else {
                    // Displaying error if permission is not granted
                    if (permissionName == Manifest.permission.READ_MEDIA_IMAGES) {
                        showErrorSnackBar(resources.getString(R.string.permission_storage_required))
                    }
                    if (permissionName == Manifest.permission.POST_NOTIFICATIONS) {
                        showErrorSnackBar(resources.getString(R.string.permission_notification_required))
                    }
                }
            }
        }

    fun showErrorSnackBar(message: String) {
        val snackBar =
            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
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
        val snackBar =
            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
        val snackBarView = snackBar.view
        snackBarView.setBackgroundColor(
            ContextCompat.getColor(
                this@BaseActivity,
                R.color.green_700
            )
        )
        snackBar.show()
    }

    // function to show and alert for notification
    fun showAlert(time: Long, title: String, message: String) {
        val date = Date(time)
        val dateFormat = DateFormat.getLongDateFormat(applicationContext)
        val timeFormat = DateFormat.getTimeFormat(applicationContext)

        AlertDialog.Builder(this)
            .setTitle("Notification Scheduled")
            .setMessage(
                "Title: " + title +
                        "\nMessage: " + message +
                        "\nAt: " + dateFormat.format(date) + " " + timeFormat.format(date)
            )
            .setPositiveButton("Okay") { _, _ -> }
            .show()
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
        Handler(Looper.getMainLooper()).postDelayed(
            {
                doubleBackToExitPressedOnce = false
            },
            2000
        )
    }

}