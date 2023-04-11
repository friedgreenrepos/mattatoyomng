package com.example.mattatoyomng.activities

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.example.mattatoyomng.R
import com.example.mattatoyomng.databinding.ActivityUpdateProfileBinding
import com.example.mattatoyomng.firebase.FirestoreClass
import com.example.mattatoyomng.models.User
import com.example.mattatoyomng.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

class UpdateProfileActivity : BaseActivity() {

    private val TAG: String = "UpdateProfileActivity"

    // binding
    private lateinit var binding: ActivityUpdateProfileBinding

    // toolbar
    private lateinit var toolbarProfileActivity: Toolbar

    // User info global variable
    private lateinit var userInfo: User

    // Global variable for URI of a selected image from phone storage.
    private var profilePicUri: Uri? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // toolbar setup
        toolbarProfileActivity = binding.toolbarProfileActivity
        setupActionBar()

        // load user data
        FirestoreClass().loadUserData(this@UpdateProfileActivity)

        binding.userUpdateProfilePicIV.setOnClickListener {
            requestStoragePermission(view = it)
        }

        binding.updateProfileBTN.setOnClickListener {
            updateUserInfo()
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(toolbarProfileActivity)
        val actionBar = supportActionBar

        // add back button and back navigation functionality and remove title
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.arrow_back_white)
            actionBar.setDisplayShowTitleEnabled(false)
        }
        toolbarProfileActivity.setNavigationOnClickListener { onBackPressed() }
    }

    // Function to populate user profile with user info
    fun setUserDataInUI(user: User) {
        userInfo = user
        Log.d(TAG,"CALLING setUserDataInUI...")
        Log.d(TAG, "User profile pic URL/URI = ${user.profilePic}")
        try {
            Glide
                .with(this@UpdateProfileActivity)
                .load(Uri.parse(user.profilePic))
                .centerCrop()
                .placeholder(R.drawable.user_white_80)
                .into(binding.userUpdateProfilePicIV)
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, "Error loading profile pic in nav drawer: ${e.message}")
        }


        binding.profileNameET.setText(user.name)
        binding.profileUsernameET.setText(user.username)
        binding.profileEmailET.setText(user.email)
    }

    // ActivityResultLauncher to open gallery
    private val openGalleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // get the returned result from the lambda and check the resultCode and the data returned
            // if the data is not null reference the imageView from the layout
            if (result.resultCode == RESULT_OK && result.data != null) {
                val userProfilePic: ImageView = binding.userUpdateProfilePicIV
                // save URI of selected image
                profilePicUri = result.data?.data!!

                Log.d(TAG, "in openGalleryLauncher...")
                Log.d(TAG, "Glide load URI: ${Uri.parse(profilePicUri.toString())}")

                // Load user image into ImageView
                try {
                    Glide
                        .with(this@UpdateProfileActivity)
                        .load(Uri.parse(profilePicUri.toString()))
                        .centerCrop()
                        .placeholder(R.drawable.user_white_80)
                        .into(userProfilePic)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.e(TAG, "Error loading profile pic: ${e.message}")
                }
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
                        Log.d(TAG, "Permission granted to open gallery...")
                    }
                } else {
                    // Displaying toast if storage permission is not granted
                    if (permissionName == Manifest.permission.READ_MEDIA_IMAGES) {
                        showErrorSnackBar(resources.getString(R.string.permission_storage_required))
                    }
                }
            }
        }

    // Method to request storage permission
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestStoragePermission(view: View) {
        // Check if the permission was denied and show rationale
        if (
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            )
        ) {
            // show snackbar to tell the user why they need to allow permission request
            showInfoSnackBar(getString(R.string.permission_storage_required))
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

    // Method to get the extension of selected image.
    private fun getFileExtension(uri: Uri?): String? {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri!!))
    }

    // Method to upload user image to firebase storage. Also sets image URI global var
    private fun uploadUserProfilePic() {
        // show progress bar
        binding.profilePB.visibility = View.VISIBLE

        val imageFilename =
            "user_" + System.currentTimeMillis() + "." + getFileExtension(profilePicUri)

        if (profilePicUri != null) {
            // save image to Firebase Storage "user_profile_pics" folder
            val sRef: StorageReference = FirebaseStorage.getInstance().reference
                .child("user_profile_pics")
                .child(imageFilename)
            sRef.putFile(profilePicUri!!)
                .addOnSuccessListener {
                    // hide progress bar
                    binding.profilePB.visibility = View.INVISIBLE
                    Log.i(TAG, "User pic URL = ${profilePicUri.toString()}")
                }
                .addOnFailureListener { exception ->
                    // if operation fails hide progress bar and show Snackbar with error
                    binding.profilePB.visibility = View.INVISIBLE
                    Log.e(TAG, "ERROR: ${exception.message}")
                    showErrorSnackBar("ERROR uploading user profile picture")
                }
        }
    }

    private fun updateUserInfo() {
        if (profilePicUri != null) {
            uploadUserProfilePic()
        }

        val userHashMap = HashMap<String, Any>()
        val name = binding.profileNameET.text.toString()
        val username = binding.profileUsernameET.text.toString()
        val profilePicUrl = profilePicUri.toString()

        if (profilePicUrl.isNotEmpty() && profilePicUrl != userInfo.profilePic) {
            userHashMap[Constants.PROFILE_PIC] = profilePicUrl
        }

        if (name != userInfo.name) {
            userHashMap[Constants.NAME] = name
        }

        if (username != userInfo.username) {
            userHashMap[Constants.MOBILE] = username
        }

        // Update the data in the database.
        FirestoreClass().updateUserProfileDataActivity(this@UpdateProfileActivity, userHashMap)
    }

    fun profileUpdateSuccess() {
        binding.profilePB.visibility = View.INVISIBLE
        finish()
    }
}
