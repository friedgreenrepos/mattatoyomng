package com.example.mattatoyomng.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp

data class Event(
    val title: String = "",
    val description: String = "",
    val owner: String = "",
    val date: Timestamp? = null,
    val eventImgURL: String = "",
    val documentID: String = ""
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readParcelable(Timestamp::class.java.classLoader),
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(owner)
        parcel.writeParcelable(date, flags)
        parcel.writeString(eventImgURL)
        parcel.writeString(documentID)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Event> {
        override fun createFromParcel(parcel: Parcel): Event = Event(parcel)
        override fun newArray(size: Int): Array<Event?> = arrayOfNulls(size)
    }
}