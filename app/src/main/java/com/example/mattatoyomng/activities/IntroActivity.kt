package com.example.mattatoyomng.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import com.example.mattatoyomng.databinding.ActivityIntroBinding
import com.example.mattatoyomng.firebase.FirestoreClass
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class IntroActivity : BaseActivity() {

    // binding
    private lateinit var binding: ActivityIntroBinding

    // firebase auth
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
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

        // Check if user is logged in. If so, go to MainActivity and finish IntroActivity
        val currentUserID = FirestoreClass().getCurrentUserID()
        if (currentUserID.isNotEmpty()) {
            val intent = Intent(this@IntroActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.apply {
            goToLoginBTN.setOnClickListener{
                val intent = Intent(this@IntroActivity, LoginActivity::class.java)
                startActivity(intent)
            }

            goToRegisterBTN.setOnClickListener {
                val intent = Intent(this@IntroActivity, RegisterActivity::class.java)
                startActivity(intent)
            }
        }

    }
}