package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.error_handler

import android.content.Context
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.showToast
import com.google.firebase.storage.StorageException
import timber.log.Timber

// https://firebase.google.com/docs/storage/android/handle-errors

class StorageExceptionHandler(private val context: Context) {

    fun storageExceptionHandling(e: StorageException) {
        Timber.e("${context.getString(R.string.app_name)} StorageException: ${e.message}")
        e.printStackTrace()

        when(e.errorCode) {
            StorageException.ERROR_BUCKET_NOT_FOUND ->
                showToast(context, context.getString(R.string.bucket_not_found))
            StorageException.ERROR_CANCELED ->
                showToast(context, context.getString(R.string.canceled))
            StorageException.ERROR_INVALID_CHECKSUM ->
                showToast(context, context.getString(R.string.invalid_checksum))
            StorageException.ERROR_NOT_AUTHENTICATED ->
                showToast(context, context.getString(R.string.not_authenticated))
            StorageException.ERROR_NOT_AUTHORIZED ->
                showToast(context, context.getString(R.string.not_authorized))
            StorageException.ERROR_OBJECT_NOT_FOUND ->
                showToast(context, context.getString(R.string.object_not_found))
            StorageException.ERROR_PROJECT_NOT_FOUND ->
                showToast(context, context.getString(R.string.project_not_found))
            StorageException.ERROR_QUOTA_EXCEEDED ->
                showToast(context, context.getString(R.string.quota_exceed))
            StorageException.ERROR_RETRY_LIMIT_EXCEEDED ->
                showToast(context, context.getString(R.string.retry_limit_exceeded))
            else -> showToast(context, context.getString(R.string.unknown_error_has_occurred))
        }
    }
}