package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

fun showToast(context: Context, text: String, duration: Int = Toast.LENGTH_LONG) {
    CoroutineScope(Dispatchers.Main).launch {
        Toast.makeText(context, text, duration).show()
    }
}

fun getCurrentTime(): String =
    SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())