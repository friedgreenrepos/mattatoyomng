package com.example.mattatoyomng.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import com.example.mattatoyomng.databinding.ActivityIntroBinding
import com.example.mattatoyomng.firebase.FirebaseAuthClass
import com.example.mattatoyomng.firebase.FirestoreClass
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class IntroActivity : BaseActivity() {

    // binding
    private lateinit var binding: ActivityIntroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set activity to full screen
        window.insetsController?.hide(WindowInsets.Type.statusBars())

        // Check if user is logged in. If so, go to MainActivity and finish IntroActivity
        val currentUserID = FirebaseAuthClass().getCurrentUserID()
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