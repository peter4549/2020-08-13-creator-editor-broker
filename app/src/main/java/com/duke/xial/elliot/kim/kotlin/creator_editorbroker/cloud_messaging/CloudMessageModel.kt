package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.cloud_messaging

import com.google.firebase.firestore.ListenerRegistration
import com.google.gson.Gson

data class CloudMessageModel(val registration_ids: List<String>) {

    var data: Data = Data()
    var notification: Notification = Notification()

    @Suppress("PropertyName")
    class Notification {
        var click_action: String = ""
        var title: String = ""
        var body: String = ""
        var tag: String = ""
    }

    class Data {
        var message: String = ""
        var roomId: String = ""
        var senderPublicName: String = ""
    }
}