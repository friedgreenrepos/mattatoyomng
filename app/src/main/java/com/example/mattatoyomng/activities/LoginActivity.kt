package com.example.mattatoyomng.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.mattatoyomng.R
import com.example.mattatoyomng.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : BaseActivity() {
    // binding
    private lateinit var binding: ActivityLoginBinding

    // firebase auth
    private lateinit var auth: FirebaseAuth

    // toolbar
    private lateinit var toolbarLoginActivity: Toolbar

    private var TAG: String = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
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
        toolbarLoginActivity = binding.toolbarLoginActivity
        setupActionBar()

        // Initialize Firebase Auth
        auth = Firebase.auth

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
            actionBar.setHomeAsUpIndicator(R.drawable.arrow_back)
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
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success")
                        userLoginSuccess()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        showErrorSnackBar(
                            "${resources.getString(R.string.auth_failed)}: " +
                                    "${task.exception!!.message}"
                        )
                    }
                }
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

    private fun userLoginSuccess() {
        // hide progress bar
        binding.loginPB.visibility = View.INVISIBLE
        showInfoSnackBar(resources.getString(R.string.login_successfully))
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}