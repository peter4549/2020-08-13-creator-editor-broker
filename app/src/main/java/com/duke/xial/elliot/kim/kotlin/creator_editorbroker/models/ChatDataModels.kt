package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models

data class ChatRoomModel(val creationTime: Long,
                         val roomId: String,
                         val unreadCounter: MutableMap<String, Long>,
                         val userIds: MutableList<String>,
                         val users: MutableList<UserModel>,
                         var lastMessage: ChatMessageModel) {

    companion object {
        const val KEY_CHAT_MESSAGES = "chatMessages"
        const val KEY_LAST_MESSAGE = "lastMessage"
        const val KEY_ROOM_ID = "roomId"
        const val KEY_UNREAD_COUNTER = "unreadCounter"
        const val KEY_USER_IDS = "userIds"
    }
}

data class ChatMessageModel(val message: String,
                            val readUsers: MutableList<String>,
                            val senderId: String,
                            val time: Long) {

    companion object {
        const val KEY_READ_USERS = "readUsers"
        const val KEY_TIME = "time"
    }
}