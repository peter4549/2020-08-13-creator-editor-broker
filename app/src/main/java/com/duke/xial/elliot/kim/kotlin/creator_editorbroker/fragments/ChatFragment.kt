package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.activities.MainActivity
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.adapters.BaseRecyclerViewAdapter
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.adapters.GridLayoutManagerWrapper
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.cloud_messaging.CloudMessageModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.constants.FireStore.COLLECTION_CHAT_ROOMS
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.constants.FireStore.COLLECTION_MESSAGES
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.constants.FireStore.COLLECTION_USERS
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.constants.PR_LIST
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.ChatMessageModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.ChatMessageModel.Companion.KEY_READ_USERS
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.ChatMessageModel.Companion.KEY_TIME
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.ChatRoomModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.ChatRoomModel.Companion.KEY_LAST_MESSAGE
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.ChatRoomModel.Companion.KEY_USER_IDS
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.UserInformationModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.retrofit2.CloudMessaging
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.*
import com.google.firebase.firestore.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.fragment_chat.view.*
import kotlinx.android.synthetic.main.item_view_chat_left.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class ChatFragment(private val targetUser: UserInformationModel? = null,
                   private val existingChatRoomId: String? = null): Fragment() {

    private lateinit var chatMessagesCollectionReference: CollectionReference
    private lateinit var chatMessagesRecyclerViewAdapter: ChatMessageRecyclerViewAdapter
    private lateinit var chatRoomsCollectionReference: CollectionReference
    private lateinit var chatRoomDocumentReference: DocumentReference
    private lateinit var currentChatRoom: ChatRoomModel
    private lateinit var listenerRegistration: ListenerRegistration
    private lateinit var previousChatMessages: MutableList<ChatMessageModel>
    private lateinit var userInformation: UserInformationModel
    private lateinit var usersCollectionReference: CollectionReference
    private val gson = Gson()
    private var chatRoomCreated = false
    private var chatMessagesIsInitialized = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        chatRoomsCollectionReference = FirebaseFirestore.getInstance().collection(
            COLLECTION_CHAT_ROOMS
        )
        usersCollectionReference = FirebaseFirestore.getInstance().collection(
            COLLECTION_USERS
        )

        userInformation = MainActivity.currentUserInformation!!
        if (existingChatRoomId == null)
            startChatWithCheckExistingChatRoom(targetUser?.uid!!, view)
        else {
            openExistingChatRoom(existingChatRoomId, view)
        }

        view.button_send.setOnClickListener {
            if (edit_text_chat_message.text.isBlank())
                showToast(requireContext(), getString(R.string.please_enter_your_message))
            else {
                if (chatRoomCreated) {
                    sendMessage(edit_text_chat_message.text.toString())
                } else {
                    createChatRoom(edit_text_chat_message.text.toString(), view)
                }

                edit_text_chat_message.text.clear()
            }
        }

        return view
    }

    override fun onPause() {
        if (::listenerRegistration.isInitialized)
            listenerRegistration.remove()
        super.onPause()
    }

    private fun openExistingChatRoom(roomId: String, view: View) {
        chatRoomsCollectionReference.document(roomId).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot != null && snapshot.data != null)
                    currentChatRoom = gson.fromJson(JSONObject(snapshot.data!!).toString(),
                        ChatRoomModel::class.java)
                initializeRecyclerView(view)
                chatRoomCreated = true
                MainActivity.currentChatRoomId = currentChatRoom.roomId
            }
            .addOnFailureListener {
                errorHandling(it, getString(R.string.chat_room_not_found))
            }
    }

    private fun errorHandling(e: Exception, toastMessage: String? = null, throwing: Boolean = false) {
        (requireActivity() as MainActivity).errorHandler.errorHandling(e, toastMessage, throwing)
    }

    private fun createChatRoom(message: String, view: View) {
        val creationTime = getCurrentTime()
        val roomId = hashString(userInformation.uid + targetUser?.uid + creationTime).chunked(32)[0]
        val firstChatMessage = generateChatMessage(message, creationTime)
        currentChatRoom = generateChatRoom(
            creationTime, firstChatMessage, roomId,
            mutableListOf(userInformation.uid, targetUser?.uid!!),
            mutableListOf(userInformation, targetUser)
        )

        MainActivity.currentUserInformation?.chatRoomIds?.add(roomId)

        chatRoomsCollectionReference
            .document(currentChatRoom.roomId)
            .set(currentChatRoom).addOnSuccessListener {
                chatRoomsCollectionReference
                    .document(currentChatRoom.roomId)
                    .collection(COLLECTION_MESSAGES)
                    .add(firstChatMessage)
                    .addOnSuccessListener {
                        initializeRecyclerView(view)
                        if (targetUser.pushToken != null)
                            sendCloudMessage(mutableListOf(targetUser.pushToken!!), message)
                        chatRoomCreated = true
                        MainActivity.currentChatRoomId = currentChatRoom.roomId
                    }
                    .addOnFailureListener {
                        errorHandling(it, getString(R.string.failed_to_create_chat_room))
                    }
            }.addOnFailureListener {
                errorHandling(it, getString(R.string.failed_to_create_chat_room))
            }
    }

    private fun startChatWithCheckExistingChatRoom(targetUserId: String, view: View) {
        chatRoomsCollectionReference
            .whereArrayContains(KEY_USER_IDS, targetUserId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot != null) {
                    for (document in snapshot.documents) {
                        if (document != null && document.data != null) {
                            val chatRoom = getChatRoom(document.data!!)
                            if (chatRoom.userIds.contains(userInformation.uid) &&
                                chatRoom.userIds.count() == 2) {
                                currentChatRoom = chatRoom
                                initializeRecyclerView(view)
                                chatRoomCreated = true
                                MainActivity.currentChatRoomId = currentChatRoom.roomId
                            }
                        }
                    }
                } else
                    errorHandling(NullPointerException("data is null"),
                        getString(R.string.uid_not_found))
            }
            .addOnFailureListener { e ->
                errorHandling(e, getString(R.string.uid_not_found))
            }
    }

    private fun initializeRecyclerView(view: View) {
        chatMessagesRecyclerViewAdapter = ChatMessageRecyclerViewAdapter()
        CoroutineScope(Dispatchers.Main).launch {
            view.recycler_view_chat_messages.apply {
                adapter = chatMessagesRecyclerViewAdapter
                layoutManager = GridLayoutManagerWrapper(requireContext(), 1).apply {
                    reverseLayout = true
                }
            }
        }
    }

    private fun sendCloudMessage(pushTokens: List<String>, message: String) { // 이 함수, 통일할 것.
        val cloudMessage = CloudMessageModel(pushTokens)
        cloudMessage.notification.title = userInformation.publicName
        cloudMessage.notification.body = message
        cloudMessage.notification.click_action = "action.ad.astra.cloud.message.click"
        cloudMessage.notification.tag = currentChatRoom.roomId

        cloudMessage.data.message = message
        cloudMessage.data.roomId = currentChatRoom.roomId
        cloudMessage.data.senderPublicName = userInformation.publicName
        CloudMessaging.getCloudMessagingService().request(requestBody = (cloudMessage))
            .enqueue(object: Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful)
                    println("$TAG: cloud message sending success")
                else {
                    errorHandling(Exception("failed to send cloud message"), getString(R.string.failed_to_send_cloud_message))
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                (requireActivity() as MainActivity).errorHandler.errorHandling(t)
            }
        })
    }

    private fun generateChatMessage(message: String, time: Long) =
        ChatMessageModel(
            message = message,
            readUsers = mutableListOf(userInformation.uid),
            senderId = userInformation.uid,
            time = time
        )

    private fun generateChatRoom(
        creationTime: Long,
        lastMessage: ChatMessageModel, roomId: String, userIds: MutableList<String>,
        users: MutableList<UserInformationModel>
    ) =
        ChatRoomModel(
            creationTime = creationTime,
            lastMessage = lastMessage,
            roomId = roomId,
            userIds = userIds,
            users = users
        )

    private fun getChatRoom(map: Map<*, *>) =
        gson.fromJson(JSONObject(map).toString(), ChatRoomModel::class.java)

    private fun sendMessage(message: String) {
        button_send.isEnabled = false
        val newChatMessage = generateChatMessage(message, getCurrentTime())
        //val newPosition = currentChatRoom.chatMessages.count() // 리사이클러뷰의 카운트로 선정.

        chatMessagesCollectionReference.add(newChatMessage)
            .addOnSuccessListener {
                button_send.isEnabled = true
                val pushTokens = currentChatRoom.users
                    .filter { it.pushToken != userInformation.pushToken }
                    .mapNotNull { it.pushToken }
                sendCloudMessage(pushTokens, message)
                chatRoomsCollectionReference.document(currentChatRoom.roomId)
                    .update(mapOf(KEY_LAST_MESSAGE to currentChatRoom.lastMessage))
                    .addOnSuccessListener {
                        println("$TAG: last message updated")
                    }
                    .addOnFailureListener {
                        errorHandling(it)
                    }
            }
            .addOnFailureListener {
                button_send.isEnabled = true
                errorHandling(it, getString(R.string.failed_to_send_message))
            }

        /*
        previousChatMessages = currentChatRoom.chatMessages.map { it.copy() }.toMutableList()
        currentChatRoom.chatMessages.add(newPosition, newChatMessage)
        currentChatRoom.lastMessage = newChatMessage

        chatRoomDocumentReference
            .update(mapOf(KEY_CHAT_MESSAGES to currentChatRoom.chatMessages))
            .addOnSuccessListener {
                button_send.isEnabled = true
                val pushTokens = currentChatRoom.users
                    .filter { it.pushToken != userInformation.pushToken }
                    .mapNotNull { it.pushToken }
                sendCloudMessage(pushTokens, message)
            }
            .addOnFailureListener {
                errorHandling(it, getString(R.string.failed_to_send_message))
                button_send.isEnabled = true
            }

         */

        // 도큐먼트 레퍼에 에드, 그러면 밑에 등록된 리스너가 알아서 에드하는 식으로.

    }

    override fun onStop() {
        MainActivity.currentChatRoomId = null
        super.onStop()
    }

    inner class ChatMessageRecyclerViewAdapter(layoutId: Int = R.layout.item_view_chat_left):
        BaseRecyclerViewAdapter<ChatMessageModel>(layoutId) {

        private val myChatMessage = 0
        private val othersChatMessage = 1
        // Key: uid
        // Pair: (publicName, profileImageUri)
        private val userNameProfileUriMap =
            mutableMapOf<String, Pair<String, String?>>()

        init {
            setChatRoomUsersInformation()
            setChatMessageSnapshotListener()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return if (viewType == othersChatMessage)
                super.onCreateViewHolder(parent, viewType)
            else {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_view_chat_right, parent, false)
                ViewHolder(view)
            }
        }

        override fun getItemViewType(position: Int): Int {
            return if (items[position].senderId == userInformation.uid) {
                myChatMessage
            } else {
                othersChatMessage
            }
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val chatMessage = items[position]
            val unreadCount = currentChatRoom.userIds.count() - chatMessage.readUsers.count()

            when (chatMessage.senderId) {
                userInformation.uid -> {
                    holder.view.text_view_message.text = chatMessage.message
                    holder.view.text_view_time.text = chatMessage.time.toLocalTimeString()
                    holder.view.text_view_unread_count.text = unreadCount.toString()
                }
                else -> {
                    setImage(holder.view.image_view_profile, userNameProfileUriMap[chatMessage.senderId]?.second)
                    holder.view.text_view_public_name.text = userNameProfileUriMap[chatMessage.senderId]?.first
                    holder.view.text_view_message.text = chatMessage.message
                    holder.view.text_view_time.text = chatMessage.time.toLocalTimeString()
                    holder.view.text_view_unread_count.text = unreadCount.toString()
                }
            }
        }

        private fun setChatRoomUsersInformation() {
            for (user in currentChatRoom.users) {
                userNameProfileUriMap[user.uid] = Pair(user.publicName, user.profileImageUri)
            }
        }

        private fun setChatMessageSnapshotListener() {
            chatMessagesCollectionReference = chatRoomsCollectionReference
                .document(currentChatRoom.roomId).collection(COLLECTION_MESSAGES)
            listenerRegistration = chatMessagesCollectionReference
                .orderBy(KEY_TIME)
                .addSnapshotListener { value, error ->
                    when {
                        error != null -> errorHandling(error,
                            getString(R.string.failed_to_load_chat_messages))
                        value == null -> errorHandling(NullPointerException("value is null"),
                            getString(R.string.failed_to_load_chat_messages))
                        else -> {
                            for (change in value.documentChanges) {
                                when (change.type) {
                                    DocumentChange.Type.ADDED -> {
                                        this.insert(getChatMessages(change.document.data,
                                            change.document.id))
                                        // 딜레이랑 예외처리.
                                        recyclerView.smoothScrollToPosition(0)
                                    }
                                    DocumentChange.Type.MODIFIED -> {
                                        this.findMessageAndUpdate(getChatMessages(change.document.data))
                                    }
                                    DocumentChange.Type.REMOVED ->
                                        this.remove(getChatMessages(change.document.data))
                                }
                            }
                        }
                    }
                }
        }

        private fun findMessageAndUpdate(chatMessageModel: ChatMessageModel) {
            val item = items.find { it.senderId == chatMessageModel.senderId
                    && it.time == chatMessageModel.time }
            val position = items.indexOf(item)

            if (position == -1)
                return

            items[position] = chatMessageModel
            notifyItemChanged(position)
        }

        private fun getChatMessages(map: Map<*, *>, documentId: String = ""): ChatMessageModel {
            val chatMessage = gson.fromJson(JSONObject(map).toString(), ChatMessageModel::class.java)
            if (chatMessage == null)
                return chatMessage!! // 뷰 타입 설정 및 변경할것.

            if (!chatMessage.readUsers.contains(userInformation.uid)) {
                if (documentId.isNotBlank()) {
                    chatMessagesCollectionReference
                        .document(documentId).update(KEY_READ_USERS,
                            FieldValue.arrayUnion(userInformation.uid))
                        .addOnSuccessListener {
                            println("$TAG: readUsers updated")
                        }
                        .addOnFailureListener {
                            errorHandling(it)
                        }
                }
            }

            return chatMessage.apply {
                readUsers.apply {
                    if (!this.contains(userInformation.uid))
                        add(userInformation.uid)
                }
            }
        }
    }

    companion object {
        private const val TAG = "ChatFragment"
    }
}