package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.error_handler

import android.content.Context
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.showToast
import com.google.android.gms.common.api.ApiException
import okhttp3.ResponseBody
import retrofit2.Response
import java.lang.Exception

class ErrorHandler(private val context: Context) {

    private var hasSpecificMessage = false

    fun errorHandling(e: Exception, toastMessage: String? = null, throwing: Boolean = false) {
        e.printStackTrace()

        when(e) {
            is ApiException -> apiExceptionHandling(e)
        }

        if (throwing)
            throw e

        if (toastMessage != null && !hasSpecificMessage)
            showToast(context, toastMessage)
    }

    fun errorHandling(t: Throwable, toastMessage: String? = null, throwing: Boolean = false) {
        t.printStackTrace()

        if (throwing)
            throw t

        if (toastMessage != null)
            showToast(context, toastMessage)
    }

    private fun apiExceptionHandling(e: ApiException) {
        println("ApiException Status Code: ${e.statusCode}")
        println("ApiException Message: ${e.message}")
    }
}

class ResponseFailedException(message: String, val response: ResponseBody): Exception(message)