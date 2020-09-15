package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities

import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

fun showToast(context: Context, text: String, duration: Int = Toast.LENGTH_LONG) {
    CoroutineScope(Dispatchers.Main).launch {
        Toast.makeText(context, text, duration).show()
    }
}

fun getCurrentTime(): Long = System.currentTimeMillis()

fun Long.toLocalTimeString(): String {
    val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)
        .apply {
            timeZone = TimeZone.getDefault()
        }
    val timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT)
        .apply {
            timeZone = TimeZone.getDefault()
        }

    val localDate = dateFormat.format(Date(this))
    val localTime = timeFormat.format(Date(this))
    return "$localDate $localTime"
}

fun String.toLocalDateString(): String? {
    val simpleDateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
    simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
    val date = simpleDateFormat.parse(this)
    simpleDateFormat.timeZone = TimeZone.getDefault()
    return if (date != null)
        simpleDateFormat.format(date)
    else
        null
}


fun String.toMilliseconds(): Long {
    return try {
        val simpleDateFormat = SimpleDateFormat("yyyyMMddHHmmss",
            Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }
        val date = simpleDateFormat.parse(this)
        date?.time ?: 1000L
    } catch (e: ParseException) {
        e.printStackTrace()
        0L
    }
}

fun String.extractNumbers() = this.filter { it.isDigit() }

fun hashString(input: String, algorithm: String = "SHA-256"): String {
    return MessageDigest.getInstance(algorithm)
        .digest(input.toByteArray())
        .fold("", { string, it -> string + "%02x".format(it) })
}

fun setImage(imageView: ImageView, uri: String?, useCache: Boolean = false) {
    if (uri == null)
        imageView.visibility = View.GONE
    else if (useCache) {
        Glide.with(imageView.context)
            .load(uri)
            .error(R.drawable.ic_baseline_sentiment_dissatisfied_80)
            .transform(CenterCrop(), RoundedCorners(8))
            .transition(DrawableTransitionOptions.withCrossFade())
            .listener(null)
            .into(imageView)
    } else {
        Glide.with(imageView.context)
            .load(uri)
            .error(R.drawable.ic_baseline_sentiment_dissatisfied_80)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .transform(CenterCrop(), RoundedCorners(8))
            .transition(DrawableTransitionOptions.withCrossFade())
            .listener(null)
            .into(imageView)
    }
}

fun enableViews(vararg views: View) {
    for (view in views) {
        view.isEnabled = true
    }
}

fun disableViews(vararg views: View) {
    for (view in views) {
        view.isEnabled = false
    }
}

fun clearViewsFocus(vararg views: View) {
    for (view in views)
        view.clearFocus()
}

fun hideKeyboard(context: Context, view: View) {
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0)
}

fun getProgressDialog(context: Context): AlertDialog {
    val builder = AlertDialog.Builder(context)
    builder.setCancelable(false)
    builder.setView(R.layout.progress_dialog)
    return builder.create()
}