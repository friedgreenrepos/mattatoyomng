package com.example.mattatoyomng

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp

data class Event(
    val title: String? = null,
    val description: String? = null,
    val date: Timestamp? = null,
    val imgUrl: String? = null){

    // Binding Adapter
    // images to display into imageviews in custom views
    object DataBindingAdapter{
        @BindingAdapter("imgUrl")
        @JvmStatic
        fun setImageByRes(imageView: ImageView, imgUrl: String){
            Glide.with(imageView.context)
                .load(imgUrl)
                .into(imageView)

        }
    }
}