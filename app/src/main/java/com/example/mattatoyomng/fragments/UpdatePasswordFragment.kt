package com.example.mattatoyomng.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mattatoyomng.R
import com.example.mattatoyomng.activities.MainActivity
import com.example.mattatoyomng.databinding.FragmentUpdatePasswordBinding
import com.example.mattatoyomng.firebase.FirestoreClass

class UpdatePasswordFragment : BaseFragment() {

    private val TAG: String = "UpdatePasswordFragment"

    private lateinit var binding: FragmentUpdatePasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentUpdatePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.updatePasswordBTN.setOnClickListener {
            updateUserPassword()
        }
    }

    private fun updateUserPassword() {
        val currentPassword = binding.currentPasswordET.text.toString()
        val newPassword = binding.newPasswordET.text.toString()
        val repeatPassword = binding.passwordRepeatET.text.toString()

        if (validateUserForm(currentPassword, newPassword, repeatPassword)) {
            binding.passwordPB.visibility = View.VISIBLE
            FirestoreClass().updateUserPassword(
                this@UpdatePasswordFragment,
                currentPassword,
                newPassword)
        }
    }

    private fun validateUserForm(currentPassword: String, newPassword: String, repeatPassword: String): Boolean {
        return when {
             currentPassword.isEmpty()-> {
                showErrorSnackBar(resources.getString(R.string.enter_current_password))
                false
            }
            newPassword.isEmpty() -> {
                showErrorSnackBar(resources.getString(R.string.enter_new_password))
                false
            }
            repeatPassword.isEmpty()-> {
                showErrorSnackBar(resources.getString(R.string.enter_confirm_password))
                false
            }
            newPassword != repeatPassword -> {
                showErrorSnackBar(resources.getString(R.string.password_not_match))
                false
            }
            else -> {
                true
            }
        }
    }

    fun authenticationSuccess(){
        Log.d(TAG, "RE-AUTH SUCCESS")
    }

    fun authenticationFail(e: Exception){
        binding.passwordPB.visibility = View.INVISIBLE
        Log.d(TAG, "RE-AUTH FAIL: $e")
        showErrorSnackBar(resources.getString(R.string.auth_failed) + ": " + e)
    }

    fun updatePasswordSuccess() {
        binding.passwordPB.visibility = View.INVISIBLE
        showInfoSnackBar(resources.getString(R.string.update_password_success))
        // re-launch main activity so that the navigation drawer updates
        val intent = Intent(this.context, MainActivity::class.java)
        startActivity(intent)
    }

    fun updatePasswordFail(e: Exception) {
        binding.passwordPB.visibility = View.INVISIBLE
        showErrorSnackBar(resources.getString(R.string.update_password_fail) + ": " + e.message)
    }
}