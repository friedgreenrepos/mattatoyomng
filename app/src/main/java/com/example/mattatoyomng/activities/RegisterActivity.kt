package com.example.mattatoyomng.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.widget.Toolbar
import com.example.mattatoyomng.R
import com.example.mattatoyomng.databinding.ActivityRegisterBinding
import com.example.mattatoyomng.firebase.FirestoreClass
import com.example.mattatoyomng.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterActivity : BaseActivity() {

    // binding
    private lateinit var binding: ActivityRegisterBinding

    // firebase auth
    private lateinit var auth: FirebaseAuth

    // toolbar
    private lateinit var toolbarRegisterActivity: Toolbar

    private var TAG: String = "RegisterActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set activity to full screen
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        // toolbar setup
        toolbarRegisterActivity = binding.toolbarRegisterActivity
        setupActionBar()

        // Initialize Firebase Auth
        auth = Firebase.auth

        binding.apply {

            // "Log in here" button is clicked. Go to LoginActivity
            loginHereTV.setOnClickListener {
                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                startActivity(intent)
            }

            // "Sign-up" button is clicked. Handle user registration.
            registerBTN.setOnClickListener {
                createUser()
            }
        }
    }

    // Method for setting up action bar:
    // add back button and back navigation functionality and remove title
    private fun setupActionBar() {
        setSupportActionBar(toolbarRegisterActivity)
        val actionBar = supportActionBar

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.arrow_back_white)
            actionBar.setDisplayShowTitleEnabled(false)
        }
        toolbarRegisterActivity.setNavigationOnClickListener { onBackPressed() }
    }

    // Method for creating User:
    // 1. Validate user form
    // 2. create User on Firebase Authentication (with email and password)
    // 3. create User on Firebase Firestore (with all info)
    private fun createUser() {
        val name: String = binding.regNameET.text.toString()
        val username: String = binding.regUsernameET.text.toString().trim { it <= ' ' }
        val email: String = binding.regEmailET.text.toString().trim { it <= ' ' }
        val password: String = binding.regPasswordET.text.toString().trim { it <= ' ' }

        if (validateUserForm(name, username, email, password)) {

            // show progress bar
            binding.registerPB.visibility = View.VISIBLE

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        val registeredEmail = firebaseUser.email!!
                        val user = User(firebaseUser.uid, name, username, registeredEmail)

                        // create new user as document on Firestore as well
                        FirestoreClass().registerUser(this@RegisterActivity, user)
                        userRegisteredSuccess()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        showErrorSnackBar("${resources.getString(R.string.auth_failed)}: ${task.exception}")
                    }
                }
        }
    }

    // Function to validate user input. Show Snackbar if input is empty.
    private fun validateUserForm(
        name: String,
        username: String,
        email: String,
        password: String
    ): Boolean {
        return when {
            TextUtils.isEmpty(name) -> {
                showErrorSnackBar(resources.getString(R.string.enter_name))
                false
            }
            TextUtils.isEmpty(username) -> {
                showErrorSnackBar(resources.getString(R.string.enter_username))
                false
            }
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar(resources.getString(R.string.enter_email))
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar(resources.getString(R.string.enter_password))
                false
            }
            else -> {
                true
            }
        }
    }

    // Function for when user signs up successfully:
    fun userRegisteredSuccess() {
        // hide progress bar
        binding.registerPB.visibility = View.INVISIBLE
        showInfoSnackBar(resources.getString(R.string.register_successfully))
        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }


}