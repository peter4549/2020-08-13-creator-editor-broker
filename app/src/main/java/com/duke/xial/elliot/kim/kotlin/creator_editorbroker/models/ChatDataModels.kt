package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models

data class ChatRoomModel(var chatMessages: MutableList<ChatMessageModel>,
                         val creationTime: String,
                         var lastMessage: String,
                         var lastMessageTime: String,
                         val roomId: String,
                         val userIds: MutableList<String>,
                         val users: MutableList<UserInformationModel>) {

    companion object {
        const val KEY_CHAT_MESSAGES = "chatMessages"
        const val KEY_ROOM_ID = "roomId"
        const val KEY_USER_IDS = "userIds"
    }
}

data class ChatMessageModel(val message: String,
                            val readUsers: MutableList<String>,
                            val senderId: String,
                            val time: String)