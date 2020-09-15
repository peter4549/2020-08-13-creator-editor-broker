package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.youtube

import android.content.Context
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.error_handler.YouTubePlayerException
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.showToast
import com.google.android.youtube.player.YouTubePlayer
import com.google.gson.Gson
import okhttp3.ResponseBody
import java.lang.Exception

class YouTubeExceptionHandler(private val context: Context) {

    fun youtubeDataApiExceptionHandling(e: YouTubeResponseFailureException, toastText: String? = null,
                                        callback: (() -> Unit)? = null) {
        callback?.invoke()
        val errorBody = Gson().fromJson(e.errorBody.string(), YouTubeErrorModels::class.java)!!
        val error = errorBody.error
        if (toastText != null)
            showToast(context, "${toastText}: ${error.getReason()}")
        printErrorLog(error)
    }

    private fun printErrorLog(error: ErrorModel) {
        println("code: ${error.code}")
        println("message: ${error.message}")
        println("domain: ${error.errors?.get(0)?.domain}")
        println("reason: ${error.errors?.get(0)?.reason}")
        println("status: ${error.status}")
    }

    private fun youtubePlayerExceptionHandling(e: YouTubePlayerException) {
        val text = when(e.reason) {
            YouTubePlayer.ErrorReason.AUTOPLAY_DISABLED ->
                context.getString(R.string.youtube_player_error_reason_autoplay_disabled)
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
            else -> context.getString(R.string.youtube_player_error_reason_unknown)
        }

        showToast(context, text)
    }

    class YouTubeResponseFailureException(message: String, val errorBody: ResponseBody): Exception(message)
}