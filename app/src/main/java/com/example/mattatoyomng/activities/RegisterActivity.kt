package com.example.mattatoyomng.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.addCallback
import android.os.Bundle
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.Toolbar
import com.example.mattatoyomng.R
import com.example.mattatoyomng.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

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

        binding.registerBTN.setOnClickListener {
            val name: String = binding.regNameET.text.toString()
            val username: String = binding.regUsernameET.toString()
            val email: String = binding.regEmailET.text.toString()
            val password: String = binding.regPasswordET.text.toString()
            createUser(email, password)
        }

        binding.loginHereTV.setOnClickListener {
            launchLoginActivity()
        }
    }
    private fun setupActionBar() {
        setSupportActionBar(toolbarRegisterActivity)
        val actionBar = supportActionBar

        // add back button and back navigation functionality and remove title
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.arrow_back)
            actionBar.setDisplayShowTitleEnabled(false)
        }
        toolbarRegisterActivity.setNavigationOnClickListener { onBackPressed() }
    }

    private fun createUser(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success")
                        val user = auth.currentUser
                        launchLoginActivity()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext,
                            R.string.auth_failed,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } else {
            Toast.makeText(
                baseContext,
                R.string.credentials_empty,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun launchLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}