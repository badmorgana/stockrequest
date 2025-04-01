package com.example.stockrequest.utils

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.stockrequest.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Extension functions to simplify common operations.
 */

/**
 * Extension to load an image from a URL into an ImageView using Glide.
 */
fun ImageView.loadImage(url: String?, @DrawableRes placeholder: Int = R.drawable.placeholder_image) {
    Glide.with(this.context)
        .load(url)
        .placeholder(placeholder)
        .error(R.drawable.error_image)
        .transition(DrawableTransitionOptions.withCrossFade())
        .centerCrop()
        .into(this)
}

/**
 * Extension to format a Date to a readable string.
 */
fun Date.formatToString(pattern: String = "MMM dd, yyyy"): String {
    val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
    return dateFormat.format(this)
}

/**
 * Extension to show a Toast from any Context.
 */
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

/**
 * Extension to hide a view (sets visibility to GONE).
 */
fun View.hide() {
    this.visibility = View.GONE
}

/**
 * Extension to show a view (sets visibility to VISIBLE).
 */
fun View.show() {
    this.visibility = View.VISIBLE
}

/**
 * Extension to toggle a view's visibility.
 */
fun View.toggleVisibility() {
    if (this.visibility == View.VISIBLE) {
        this.visibility = View.GONE
    } else {
        this.visibility = View.VISIBLE
    }
}

/**
 * Extension to safely parse a string to an integer.
 */
fun String?.toIntOrDefault(default: Int = 0): Int {
    return this?.toIntOrNull() ?: default
}

/**
 * Extension to get color based on stock request status.
 */
fun String.getStatusColor(context: Context): Int {
    return when (this) {
        Constants.STATUS_SUBMITTED -> context.getColor(R.color.status_submitted)
        Constants.STATUS_PROCESSING -> context.getColor(R.color.status_processing)
        Constants.STATUS_ORDERED -> context.getColor(R.color.status_ordered)
        Constants.STATUS_COMPLETED -> context.getColor(R.color.status_completed)
        Constants.STATUS_ON_HOLD -> context.getColor(R.color.status_on_hold)
        else -> context.getColor(R.color.status_submitted)
    }
}