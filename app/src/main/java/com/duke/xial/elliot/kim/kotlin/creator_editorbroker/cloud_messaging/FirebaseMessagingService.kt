package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.cloud_messaging

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.activities.MainActivity
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessagingService: com.google.firebase.messaging.FirebaseMessagingService() {

    private lateinit var roomId: String

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.data.isNotEmpty().let {

            val message = remoteMessage.data["message"] ?: "Message has been lost"
            val senderPublicName = remoteMessage.data["senderPublicName"]
                ?: "Sender public name has been lost"
            roomId = remoteMessage.data["roomId"] ?: ""

            if (MainActivity.currentChatRoomId != roomId)
                sendNotification(senderPublicName, message)
        }
    }

    // 여기서 업데이트 해주면됨.  업로드 로직만 짜놓을 것.
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        println("$TAG: new token: $token")
    }

    private fun sendNotification(senderPublicName: String, message: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.action = ACTION_CHAT_NOTIFICATION
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra(KEY_CHAT_ROOM_ID, roomId)

        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT)

        val channelId = CHANNEL_ID
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setContentText(message)
            .setContentTitle(senderPublicName)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.ic_round_check_circle_24)
            .setSound(defaultSoundUri)  // 사운드 조정 필요.

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                CHANNEL_TITLE,
                NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build()) // id 부분, 만약 메시지 다른데서 왓어도 정상동작할런지.
    }

    companion object {
        const val ACTION_CHAT_NOTIFICATION = "action.chat.notification"
        const val KEY_CHAT_ROOM_ID = "key_chat_room_id"
        private const val CHANNEL_ID = "notification_channel_id"
        private const val CHANNEL_TITLE = "chat_room_notification"
        private const val TAG = "FirebaseMessagingService"
    }
}