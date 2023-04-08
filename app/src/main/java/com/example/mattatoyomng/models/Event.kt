package com.example.mattatoyomng.models

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp

data class Event(
    val title: String? = null,
    val description: String? = null,
    val owner: String? = null,
    val date: Timestamp? = null,
    val eventImgURL: String? = null
) {

    // Binding Adapter - images to display into imageviews in custom views
    companion object {
        @BindingAdapter("eventImgURL")
        @JvmStatic
        fun loadImage(imageView: ImageView, imgUrl: String) {
            Glide.with(imageView.context)
                .load(imgUrl)
                .into(imageView)
        }
    }
}