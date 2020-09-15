package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.youtube

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.activities.MainActivity
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.showToast

class CustomTabsReceiver : BroadcastReceiver() { // 딱히 필요없어보임?? 더 보류./// 레알 필요없어 보인다. 오우수ㅏㅔㅅ;
    override fun onReceive(context: Context?, intent: Intent?) {
        val url = intent?.dataString!!

        if (url.startsWith("https://accounts.google.com/o/oauth2")) {
            val approvalCodeIntent = Intent(context, MainActivity::class.java)
            approvalCodeIntent.action = ACTION_APPROVAL_CODE
            approvalCodeIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            context?.startActivity(approvalCodeIntent)
        } else {
            context?.let {
                // showToast(context, context.getString(R.string.click_on_the_page_where_the_verification_code_is_displayed))
                showToast(context, "AAAAAA")
            }
        }
    }

    companion object {
        const val ACTION_APPROVAL_CODE = "action_approval_code"
    }
}