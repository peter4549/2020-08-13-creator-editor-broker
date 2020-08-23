package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.error_handler

import android.content.Context
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.showToast
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.youtube.player.YouTubePlayer
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_sign_up.*
import okhttp3.ResponseBody
import java.lang.Exception

class ErrorHandler(private val context: Context) {

    private var hasSpecificMessage = false

    fun errorHandling(e: Exception, toastMessage: String? = null, throwing: Boolean = false) {
        e.printStackTrace()

        when(e) {
            is ApiException -> apiExceptionHandling(e)
            is ResponseFailureException -> responseFailureExceptionHandling(e)
            is FirebaseException -> firebaseExceptionHandling(e)
            is YouTubePlayerException -> youtubePlayerExceptionHandling(e)
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

    private fun firebaseExceptionHandling(e: FirebaseException) {
        hasSpecificMessage = true
        when(e) {
            is FirebaseAuthInvalidCredentialsException ->
                showToast(context, context.getString(R.string.invalid_request))
            is FirebaseTooManyRequestsException ->
                showToast(context, context.getString(R.string.too_many_requests))
            else ->
                showToast(context, context.getString(R.string.verification_failed))
        }
    }

    private fun printErrorLog(error: ErrorModel) {
        println("code: ${error.code}")
        println("message: ${error.message}")
        println("domain: ${error.errors?.get(0)?.domain}")
        println("reason: ${error.errors?.get(0)?.reason}")
        println("status: ${error.status}")
    }

    private fun youtubePlayerExceptionHandling(e: YouTubePlayerException) {
        hasSpecificMessage = true
        val text = when(e.reason) {
            YouTubePlayer.ErrorReason.AUTOPLAY_DISABLED ->
                context.getString(R.string.youtube_player_error_reason_autoplay_disabled)
            YouTubePlayer.ErrorReason.BLOCKED_FOR_APP ->
                context.getString(R.string.youtube_player_error_reason_blocked_for_app)
            YouTubePlayer.ErrorReason.EMBEDDING_DISABLED ->
                context.getString(R.string.youtube_player_error_reason_embedding_disabled)
            YouTubePlayer.ErrorReason.EMPTY_PLAYLIST ->
                context.getString(R.string.youtube_player_error_reason_empty_playlist)
            YouTubePlayer.ErrorReason.INTERNAL_ERROR ->
                context.getString(R.string.youtube_player_error_reason_internal_error)
            YouTubePlayer.ErrorReason.NETWORK_ERROR ->
                context.getString(R.string.youtube_player_error_reason_network_error)
            YouTubePlayer.ErrorReason.NOT_PLAYABLE ->
                context.getString(R.string.youtube_player_error_reason_not_playable)
            YouTubePlayer.ErrorReason.PLAYER_VIEW_TOO_SMALL ->
                context.getString(R.string.youtube_player_error_reason_player_view_too_small)
            YouTubePlayer.ErrorReason.UNAUTHORIZED_OVERLAY ->
                context.getString(R.string.youtube_player_error_reason_unauthorized_overlay)
            YouTubePlayer.ErrorReason.UNEXPECTED_SERVICE_DISCONNECTION ->
                context.getString(R.string.youtube_player_error_reason_unexpected_service_disconnection)
            YouTubePlayer.ErrorReason.UNKNOWN ->
                context.getString(R.string.youtube_player_error_reason_unknown)
            YouTubePlayer.ErrorReason.USER_DECLINED_HIGH_BANDWIDTH ->
                context.getString(R.string.youtube_player_error_reason_user_declined_high_bandwidth)
            YouTubePlayer.ErrorReason.USER_DECLINED_RESTRICTED_CONTENT ->
                context.getString(R.string.youtube_player_error_reason_user_declined_restricted_content)
            else -> context.getString(R.string.unknown_error_has_occurred)
        }

        showToast(context, text)
    }

    companion object {
        private const val PERMISSION_DENIED = "PERMISSION_DENIED"
    }
}

class ResponseFailureException(message: String, val errorBody: ResponseBody): Exception(message)
class YouTubePlayerException(message: String, val reason: YouTubePlayer.ErrorReason): Exception(message)