package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities

import android.content.Context
import android.view.View
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
import java.text.SimpleDateFormat
import java.util.*

fun showToast(context: Context, text: String, duration: Int = Toast.LENGTH_LONG) {
    CoroutineScope(Dispatchers.Main).launch {
        Toast.makeText(context, text, duration).show()
    }
}

fun getCurrentTime(): String =
    SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())

fun hashString(input: String, algorithm: String = "SHA-256"): String {
    return MessageDigest.getInstance(algorithm)
        .digest(input.toByteArray())
        .fold("", { string, it -> string + "%02x".format(it) })
}

fun setImage(imageView: ImageView, uri: String?) {
    if (uri == null)
        imageView.visibility = View.GONE
    else {
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