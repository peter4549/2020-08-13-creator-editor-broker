package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.error_handler

import android.content.Context
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.showToast
import com.google.android.gms.common.api.ApiException
import com.google.gson.Gson
import okhttp3.ResponseBody
import java.lang.Exception

class ErrorHandler(private val context: Context) {

    private var hasSpecificMessage = false

    fun errorHandling(e: Exception, toastMessage: String? = null, throwing: Boolean = false) {
        e.printStackTrace()

        when(e) {
            is ApiException -> apiExceptionHandling(e)
            is ResponseFailureException -> responseFailureExceptionHandling(e)
        }

        if (throwing)
            throw e

        if (toastMessage != null && !hasSpecificMessage)
            showToast(context, toastMessage)

        hasSpecificMessage = false
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

    private fun responseFailureExceptionHandling(e: ResponseFailureException) {
        val errorBody = Gson().fromJson<ErrorBodyModel>(e.errorBody.string(), ErrorBodyModel::class.java)!!
        val error = errorBody.error
        printErrorLog(error)

        when(error.code) {
            403.0 -> {

            }
            else -> {

            }
        }
    }

    private fun printErrorLog(error: ErrorModel) {
        println("code: ${error.code}")
        println("message: ${error.message}")
        println("domain: ${error.errors?.get(0)?.domain}")
        println("reason: ${error.errors?.get(0)?.reason}")
        println("status: ${error.status}")
    }

    companion object {
        private const val PERMISSION_DENIED = "PERMISSION_DENIED"
    }
}

class ResponseFailureException(message: String, val errorBody: ResponseBody): Exception(message)

