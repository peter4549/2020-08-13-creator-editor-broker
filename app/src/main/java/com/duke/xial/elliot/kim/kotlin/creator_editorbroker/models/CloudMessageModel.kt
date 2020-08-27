package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models

class CloudMessageModel {

    var to: String = ""
    var notification: Notification = Notification()
    var data: Data = Data()

    class Data {
        var message: String = ""
        var roomId: String = ""
        var senderPublicName: String = ""
    }

    class Notification {
        var click_action: String = "action_test"
        var title: String = ""
        var text: String = ""
    }
}