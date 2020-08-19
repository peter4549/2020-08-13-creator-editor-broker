package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.youtube

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.activities.MainActivity
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.showToast

class CustomTabsReceiver : BroadcastReceiver() { // 딱히 필요없어보임??
    override fun onReceive(context: Context?, intent: Intent?) {
        val url = intent?.dataString!!

        if (url.startsWith("https://accounts.google.com/o/oauth2")) {
            val approvalCodeIntent = Intent(context, MainActivity::class.java)
            approvalCodeIntent.action = ACTION_APPROVAL_CODE
            approvalCodeIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            context?.startActivity(approvalCodeIntent)
        } else {
            context?.let {
                showToast(context, "인증 코드가 표시된 페이지에서 클릭하십시오.")
            }
        }
    }

    companion object {
        const val ACTION_APPROVAL_CODE = "action_approval_code"
    }
}