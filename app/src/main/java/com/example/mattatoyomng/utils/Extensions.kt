package com.example.mattatoyomng.utils

import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.*
import androidx.core.content.ContextCompat
import com.example.mattatoyomng.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

fun Context.createDialog(layout: Int, cancelable: Boolean): Dialog {
    val dialog = Dialog(this, android.R.style.Theme_Dialog)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(layout)
    dialog.window?.setGravity(Gravity.CENTER)
    dialog.window?.setLayout(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.WRAP_CONTENT
    )
    dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.setCancelable(cancelable)
    return dialog
}

fun ChipGroup.addChip(
    text: String,
    isTouchTargetSize: Boolean = false,
    closeIconListener: View.OnClickListener? = null
) {
    val chip: Chip = LayoutInflater.from(context).inflate(R.layout.item_chip, null, false) as Chip
    chip.text = if (text.length > 9) text.substring(0, 9) + "..." else text
    chip.isClickable = false
    chip.setEnsureMinTouchTargetSize(isTouchTargetSize)
    if (closeIconListener != null) {
        chip.closeIcon = ContextCompat.getDrawable(context, R.drawable.ic_cancel)
        chip.isCloseIconVisible = true
        chip.setOnCloseIconClickListener(closeIconListener)
    }
    addView(chip)
}

fun ChipGroup.addChipNoClose(
    text: String,
    isTouchTargetSize: Boolean = false,
    closeIconListener: View.OnClickListener? = null
) {
    val chip: Chip = LayoutInflater.from(context).inflate(R.layout.item_chip, null, false) as Chip
    chip.text = if (text.length > 9) text.substring(0, 9) + "..." else text
    chip.isClickable = false
    chip.setEnsureMinTouchTargetSize(isTouchTargetSize)
    addView(chip)
}

val Int.dpToPx: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()