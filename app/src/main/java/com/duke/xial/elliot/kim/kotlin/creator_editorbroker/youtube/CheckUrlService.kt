package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.youtube

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class CheckUrlService: Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val url = intent?.dataString ?: ""
        if (url.startsWith("https://accounts.google.com/o/oauth2")) {
            val code = intent?.data?.getQueryParameter("approvalCode") ?: INVALID_CODE
            sendBroadcast(code)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun sendBroadcast(code: String) {
        val intent = Intent(ACTION_CORRECT_URL)
        intent.putExtra(KEY_AUTHORIZATION_CODE, code)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        println("$TAG: authorization code sent")
        stopSelf()
    }

    companion object {
        const val ACTION_CORRECT_URL = "check.url.service.action.correct.url"
        const val INVALID_CODE = "invalid_code"
        const val KEY_AUTHORIZATION_CODE = "key_authorization_code"
        private const val TAG = "CheckUrlService"
    }
}