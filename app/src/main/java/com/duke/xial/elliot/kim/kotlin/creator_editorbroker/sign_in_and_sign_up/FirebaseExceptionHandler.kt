package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.sign_in_and_sign_up

import android.content.Context
import android.widget.Toast
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.showToast
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import timber.log.Timber


class FirebaseExceptionHandler(private val context: Context) {

    private val authErrorCodes = arrayOf(
        "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL",
        "ERROR_CREDENTIAL_ALREADY_IN_USE",
        "ERROR_CUSTOM_TOKEN_MISMATCH",
        "ERROR_EMAIL_ALREADY_IN_USE",
        "ERROR_INVALID_CREDENTIAL",
        "ERROR_INVALID_CUSTOM_TOKEN",
        "ERROR_INVALID_EMAIL",
        "ERROR_INVALID_USER_TOKEN",
        "ERROR_OPERATION_NOT_ALLOWED",
        "ERROR_REQUIRES_RECENT_LOGIN",
        "ERROR_USER_DISABLED",
        "ERROR_USER_MISMATCH",
        "ERROR_USER_NOT_FOUND",
        "ERROR_USER_TOKEN_EXPIRED",
        "ERROR_WEAK_PASSWORD",
        "ERROR_WRONG_PASSWORD"
    )
    private val authErrorCodeFunctionMap = mutableMapOf<String, () -> Unit>()
    private val authErrorCodeToastMap: Map<String, String> = mapOf(
        "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" to context.getString(R.string.account_exists_with_different_credential),
        "ERROR_CREDENTIAL_ALREADY_IN_USE" to context.getString(R.string.credential_already_in_use),
        "ERROR_CUSTOM_TOKEN_MISMATCH" to context.getString(R.string.custom_token_mismatch),
        "ERROR_EMAIL_ALREADY_IN_USE" to context.getString(R.string.email_already_in_use),
        "ERROR_INVALID_CREDENTIAL" to context.getString(R.string.invalid_credential),
        "ERROR_INVALID_CUSTOM_TOKEN" to context.getString(R.string.invalid_custom_token),
        "ERROR_INVALID_EMAIL" to context.getString(R.string.invalid_email),
        "ERROR_INVALID_USER_TOKEN" to context.getString(R.string.invalid_user_token),
        "ERROR_OPERATION_NOT_ALLOWED" to context.getString(R.string.operation_not_allowed),
        "ERROR_REQUIRES_RECENT_LOGIN" to context.getString(R.string.requires_recent_login),
        "ERROR_USER_DISABLED" to context.getString(R.string.user_disabled),
        "ERROR_USER_MISMATCH" to context.getString(R.string.user_mismatch),
        "ERROR_USER_NOT_FOUND" to context.getString(R.string.user_not_found),
        "ERROR_USER_TOKEN_EXPIRED" to context.getString(R.string.user_token_expired),
        "ERROR_WEAK_PASSWORD" to context.getString(R.string.weak_password),
        "ERROR_WRONG_PASSWORD" to context.getString(R.string.wrong_password)
    )
    private val unknownErrorMessage = context.getString(R.string.unknown_error_message)

    fun exceptionHandling(e: FirebaseException, showToast: Boolean = true, `throw`: Boolean = false) {
        when (e) {
            is FirebaseAuthException -> authExceptionHandling(e, showToast, `throw`)
            is FirebaseTooManyRequestsException -> {
                showToast(context, context.getString(R.string.too_many_requests))
                Timber.e("${context.getString(R.string.app_name)} FirebaseTooManyRequestsException: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun authExceptionHandling(e: FirebaseAuthException, showToast: Boolean = true, `throw`: Boolean = false) {
        if (`throw`)
            throw e

        Timber.e("${context.getString(R.string.app_name)} FirebaseAuthException: ${e.message}")
        e.printStackTrace()

        invokeErrorFunction(e.errorCode)

        if (showToast)
            showToast(context, authErrorCodeToastMap[e.errorCode] ?: unknownErrorMessage, Toast.LENGTH_SHORT)
    }

    fun setErrorFunction(key: String, errorFunction: () -> Unit) {
        authErrorCodeFunctionMap[key] = errorFunction
    }

    private fun invokeErrorFunction(key: String) {
        authErrorCodeFunctionMap[key]?.invoke()
    }
}