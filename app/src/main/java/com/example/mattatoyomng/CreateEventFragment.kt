package com.example.mattatoyomng

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mattatoyomng.databinding.FragmentCreateEventBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage


class CreateEventFragment : Fragment() {

    private lateinit var binding: FragmentCreateEventBinding

    // user credentials
    var currentUserID: String = ""
    var currentUserName: String = ""

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private var storageReference = FirebaseStorage.getInstance().reference
    private var db = FirebaseFirestore.getInstance()
    private var collectionReference: CollectionReference = db.collection("events")

    private lateinit var imageUri: Uri


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCreateEventBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()

        binding.apply {
            // hide progress bar
            createEventPB.visibility = View.INVISIBLE
            if (User.instance != null){
                currentUserID = User.instance!!.userid.toString()
                currentUserName = User.instance!!.name.toString()
            }
            ownerNameTV.text = currentUserName

        }

        return binding.root
    }
}