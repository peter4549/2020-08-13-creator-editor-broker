package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.error_handler

import android.content.Context
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.showToast
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.youtube.player.YouTubePlayer
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.StorageException
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_sign_up.*
import okhttp3.ResponseBody
import java.lang.Exception

class ErrorHandler(private val context: Context) {

    private var hasSpecificMessage = false

    fun errorHandling(e: Exception, toastMessage: String? = null, throwing: Boolean = false) {
        val appName = context.getString(R.string.app_name)
        println("$appName exception")
        e.printStackTrace()

        when(e) {
            is ApiException -> apiExceptionHandling(e)
            is ResponseFailureException -> responseFailureExceptionHandling(e)
            is FirebaseException -> firebaseExceptionHandling(e)
            is YouTubePlayerException -> youtubePlayerExceptionHandling(e)
            is StorageException -> storageExceptionHandling(e)
            is FirebaseFirestoreException -> fireStoreExceptionHandling(e)
        }

        if (throwing)
            throw e

        if (toastMessage != null && !hasSpecificMessage)
            showToast(context, toastMessage)

        hasSpecificMessage = false
    }

    fun errorHandling(t: Throwable, toastMessage: String? = null, throwing: Boolean = false) {
        val appName = context.getString(R.string.app_name)
        println("$appName exception")
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

    // 세분화 할것.
    private fun firebaseExceptionHandling(e: FirebaseException) {
        hasSpecificMessage = true
        when(e) {
            is FirebaseAuthInvalidCredentialsException ->
                showToast(context, context.getString(R.string.invalid_request))
            is FirebaseAuthInvalidUserException ->
                showToast(context, context.getString(R.string.account_not_found))
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

    private fun storageExceptionHandling(e: StorageException) {
        when(e.errorCode) {
            StorageException.ERROR_BUCKET_NOT_FOUND ->
                showToast(context, context.getString(R.string.unknown_error_has_occurred))
            StorageException.ERROR_CANCELED ->
                showToast(context, context.getString(R.string.unknown_error_has_occurred))
            StorageException.ERROR_INVALID_CHECKSUM ->
                showToast(context, context.getString(R.string.unknown_error_has_occurred))
            StorageException.ERROR_NOT_AUTHENTICATED ->
                showToast(context, context.getString(R.string.unknown_error_has_occurred))
            StorageException.ERROR_NOT_AUTHORIZED ->
                showToast(context, context.getString(R.string.unknown_error_has_occurred))
            StorageException.ERROR_OBJECT_NOT_FOUND ->
                showToast(context, context.getString(R.string.unknown_error_has_occurred))
            StorageException.ERROR_PROJECT_NOT_FOUND ->
                showToast(context, context.getString(R.string.unknown_error_has_occurred))
            StorageException.ERROR_QUOTA_EXCEEDED ->
                showToast(context, context.getString(R.string.unknown_error_has_occurred))
            StorageException.ERROR_RETRY_LIMIT_EXCEEDED ->
                showToast(context, context.getString(R.string.unknown_error_has_occurred))
            else -> showToast(context, context.getString(R.string.unknown_error_has_occurred))
        }
    }

    // https://firebase.google.com/docs/storage/android/handle-errors
    private fun fireStoreExceptionHandling(e: FirebaseFirestoreException) {

        when(e.code) {
            FirebaseFirestoreException.Code.ABORTED -> {
                showToast(context, "작업이 중단되었습니다.")
            }
            FirebaseFirestoreException.Code.ALREADY_EXISTS -> {
                showToast(context, "데이터가 이미 존재합니다.")
            }
            FirebaseFirestoreException.Code.CANCELLED -> {
                showToast(context, "취소되었습니다.")
            }
            FirebaseFirestoreException.Code.DATA_LOSS -> {
                showToast(context, "데이터가 손실되었습니다.")
            }
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> {
                showToast(context, "기한이 만료되었습니다. 시스템 관련 작업을 진행 중이셨다면 다시 시도해보세요.")
            }
            FirebaseFirestoreException.Code.FAILED_PRECONDITION -> {
                showToast(context, "시스템이 작업을 실행할 상태가 아니기 때문에 작업이 거부되었습니다.")
            }
            FirebaseFirestoreException.Code.INTERNAL -> {
                "내부적인 오류가 발생했습니다."
            }
            FirebaseFirestoreException.Code.INVALID_ARGUMENT -> {
                "잘못된 인수가 지정되었습니다."
            }
            FirebaseFirestoreException.Code.NOT_FOUND -> {
                "요청된 데이터를 찾을 수 없습니다."
            }
            FirebaseFirestoreException.Code.OK -> {
                "작업이 성공적으로 완료되었습니다."
            }
            FirebaseFirestoreException.Code.OUT_OF_RANGE -> {
                "유효한 범위를 넘어서 작업을 시도했습니다."
            }
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                "작업을 실행할 권한이 없습니다."
            }
            FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED -> {
                "할당량이 소진 되었거나 시스템에 공간이 부족한 것일 수 있습니다."
            }
            FirebaseFirestoreException.Code.UNAUTHENTICATED -> {
                "요청에 작업에 대한 유효한 인증 자격 증명이 없습니다."
            }
            FirebaseFirestoreException.Code.UNAVAILABLE -> {
                "현재 서비스를 사용할 수 없습니다. 나중에 다시 시도해보세요."
            }
            FirebaseFirestoreException.Code.UNIMPLEMENTED -> {
                "작업이 구현되지 않았거나 지원, 활성화되지 않았습니다."
            }
            FirebaseFirestoreException.Code.UNKNOWN -> {
                "알 수 없는 오류가 발생했습니다."
            }
            else -> throw e
        }
    }

    companion object {
        private const val PERMISSION_DENIED = "PERMISSION_DENIED"
    }
}

class ResponseFailureException(message: String, val errorBody: ResponseBody): Exception(message)
class YouTubePlayerException(message: String, val reason: YouTubePlayer.ErrorReason): Exception(message)