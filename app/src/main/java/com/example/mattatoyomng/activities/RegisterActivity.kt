package com.example.mattatoyomng.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowInsets
import androidx.appcompat.widget.Toolbar
import com.example.mattatoyomng.R
import com.example.mattatoyomng.coroutines.CoroutineScopes
import com.example.mattatoyomng.databinding.ActivityRegisterBinding
import com.example.mattatoyomng.firebase.FirebaseAuthClass
import com.example.mattatoyomng.firebase.FirestoreClass
import com.example.mattatoyomng.models.User
import com.example.mattatoyomng.utils.logThread
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext

class RegisterActivity : BaseActivity(), FirestoreClass.RegisterUserCallback,
    FirebaseAuthClass.CreateUserCallback {

    // binding
    private lateinit var binding: ActivityRegisterBinding

    // toolbar
    private lateinit var toolbar: Toolbar

    private var TAG: String = "RegisterActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set activity to full screen
        window.insetsController?.hide(WindowInsets.Type.statusBars())

        // toolbar setup
        toolbar = binding.toolbarRegisterActivity
        setupActionBar()

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
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.arrow_back_blue)
            actionBar.setDisplayShowTitleEnabled(false)
        }
        toolbar.setNavigationOnClickListener { onBackPressed() }
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

            FirebaseAuthClass().createUser(this, email, password, name, username)
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
    private fun userRegisteredSuccess() {
        logThread("userRegisteredSuccess() in RegisterActivity")
        binding.registerPB.visibility = View.INVISIBLE
        showInfoSnackBar(resources.getString(R.string.register_successfully))
        finish()

    }

    private fun userRegisteredFail(e: Exception) {
        binding.registerPB.visibility = View.INVISIBLE
        showErrorSnackBar(e.message!!)
    }

    override fun onRegisterUserSuccess() {
        logThread("onRegisterUserSuccess() in RegisterActivity")
        userRegisteredSuccess()
    }

    override fun onRegisterUserError(e: Exception) {
        userRegisteredFail(e)
    }

    override fun onCreateUserSuccess(user: User) {
        logThread("onCreateUserSuccess() in RegisterActivity")
        FirestoreClass().registerUser(this, user)
    }

    override fun onCreateUserError(e: Exception) {
        userRegisteredFail(e)
    }


}