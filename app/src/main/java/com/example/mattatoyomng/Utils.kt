package com.example.mattatoyomng

import android.view.View
import com.google.android.material.snackbar.Snackbar

fun View.showSnackbar(
    view: View,
    msg: String,
    length: Int,
    actionMessage: CharSequence?,
    action: (View) -> Unit
) {
    val snackbar = Snackbar.make(view, msg, length)
    if (actionMessage != null) {
        snackbar.setAction(actionMessage) {
            action(this)
        }.show()
    } else {
        snackbar.show()
    }
}

fun stripSpaces(s: String): String {
     return s.replace(" ", "")
}
