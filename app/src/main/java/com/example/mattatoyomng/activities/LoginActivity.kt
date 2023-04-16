package com.example.mattatoyomng.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowInsets
import androidx.appcompat.widget.Toolbar
import com.example.mattatoyomng.R
import com.example.mattatoyomng.databinding.ActivityLoginBinding
import com.example.mattatoyomng.firebase.FirebaseAuthClass
import com.example.mattatoyomng.firebase.FirestoreClass
import com.example.mattatoyomng.models.User

class LoginActivity : BaseActivity(), FirebaseAuthClass.LoginUserCallback,
    FirestoreClass.GetUserDataCallback {
    // binding
    private lateinit var binding: ActivityLoginBinding

    // toolbar
    private lateinit var toolbarLoginActivity: Toolbar

    private var TAG: String = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set activity to full screen
        window.insetsController?.hide(WindowInsets.Type.statusBars())

        // toolbar setup
        toolbarLoginActivity = binding.toolbarLoginActivity
        setupActionBar()

        binding.apply {
            loginBTN.setOnClickListener() {
                loginUser()
            }

            registerHereTV.setOnClickListener() {
                val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(toolbarLoginActivity)
        val actionBar = supportActionBar

        // add back button and back navigation functionality and remove title
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.arrow_back_red)
            actionBar.setDisplayShowTitleEnabled(false)
        }
        toolbarLoginActivity.setNavigationOnClickListener { onBackPressed() }
    }

    // Function to sign-in a registered user using email and password
    private fun loginUser() {
        val email: String = binding.loginEmailET.text.toString()
        val password: String = binding.loginPasswordET.text.toString()

        if (validateUserForm(email, password)) {
            // show progress bar
            binding.loginPB.visibility = View.VISIBLE

            // sign-in user with Firebase Auth
            FirebaseAuthClass().loginUser(this, email, password)
        }
    }

    // Function to validate user input
    private fun validateUserForm(email: String, password: String): Boolean {
        return when {
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please enter email.")
                false
            }

            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Please enter password.")
                false
            }

            else -> {
                true
            }
        }
    }

    // Function to call when user successfully logs in
    private fun userLoginSuccess() {
        binding.loginPB.visibility = View.INVISIBLE
        showInfoSnackBar(resources.getString(R.string.login_successfully))
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun userLoginFail(e: Exception) {
        binding.loginPB.visibility = View.INVISIBLE
        showErrorSnackBar(e.message!!)
    }

    override fun onLoginSuccess() {
        FirestoreClass().getUserData(this)
    }


    override fun onLoginFail(e: Exception) {
        userLoginFail(e)
    }

    override fun onGetUserDataSuccess(user: User) {
        userLoginSuccess()
    }

    override fun onGetUserDataFail(e: Exception) {
        userLoginFail(e)
    }
}