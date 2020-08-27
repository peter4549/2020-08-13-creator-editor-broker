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
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.constants.FireStore.COLLECTION_CHAT_ROOMS
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.ChatMessageModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.ChatRoomModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.ChatRoomModel.Companion.KEY_CHAT_MESSAGES
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.ChatRoomModel.Companion.KEY_USER_IDS
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.UserInformationModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.getCurrentTime
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.hashString
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.showToast
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.fragment_chat.view.*
import kotlinx.android.synthetic.main.item_view_chat.view.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class ChatFragment(private val targetUserId: String? = null): Fragment() {

    private lateinit var chatMessagesRecyclerViewAdapter: ChatMessageRecyclerViewAdapter
    private lateinit var chatRoomsCollectionReference: CollectionReference
    private lateinit var chatRoomDocumentReference: DocumentReference
    private lateinit var currentChatRoom: ChatRoomModel
    private lateinit var listenerRegistration: ListenerRegistration
    private lateinit var previousChatMessages: MutableList<ChatMessageModel>
    private lateinit var userInformation: UserInformationModel
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
        userInformation = MainActivity.currentUserInformation!!
        startChatWithCheckExistingChatRoom(targetUserId!!, view)

        view.button_send.setOnClickListener {
            if (edit_text_chat_message.text.isBlank())
                showToast(requireContext(), "메시지를 입력하세요.")
            else {
                if (chatRoomCreated) {
                    sendMessage(edit_text_chat_message.text.toString())
                } else {
                    generateChatRoom(edit_text_chat_message.text.toString(), view)
                }
            }
        }

        return view
    }

    private fun generateChatRoom(message: String, view: View) {
        val creationTime = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
        val roomId = hashString(userInformation.uid + targetUserId + creationTime).chunked(32)[0]
        val firstChatMessage = generateChatMessage(message, creationTime)
        currentChatRoom = generateChatRoom(
            mutableListOf(firstChatMessage),
            creationTime, message, roomId, mutableListOf(userInformation.uid, targetUserId!!)
        )
        chatRoomsCollectionReference
            .document(roomId)
            .set(currentChatRoom)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    initializeRecyclerView(view)
                    // sendCloudMessage.
                    chatRoomCreated = true
                } else {
                    (requireActivity() as MainActivity).errorHandler
                        .errorHandling(
                            task.exception!!,
                            getString(R.string.failed_to_create_chat_room)
                        )
                }
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
                            }
                        }
                    }
                } else
                    (requireActivity() as MainActivity).errorHandler
                        .errorHandling(
                            NullPointerException("data is null"),
                            getString(R.string.uid_not_found)
                        )

            }
            .addOnFailureListener { e ->
                (requireActivity() as MainActivity).errorHandler
                    .errorHandling(e, getString(R.string.uid_not_found))
            }
    }

    private fun initializeRecyclerView(view: View) {
        chatMessagesRecyclerViewAdapter = ChatMessageRecyclerViewAdapter()
        CoroutineScope(Dispatchers.Main).launch {
            view.recycler_view_chat_messages.apply {
                adapter = chatMessagesRecyclerViewAdapter
                layoutManager = GridLayoutManagerWrapper(requireContext(), 1)
            }
        }
    }

    /*
    private fun setPushTokensAndSendCloudMessage() {
        // 그룹인지 아닌지 판단해서 보낼 것.
        // 여기서 분화할것. 만약 멤버가 2명 아래 로직.
        // 다수의 유저를 불러올때 로직. .where("UserModel.KEY_ID", "in", ["id1", "id2"]) // 일단 이론임.

        FirebaseFirestore.getInstance()
            .collection(COLLECTION_USERS).whereIn(UserInformationModel.KEY_UID, currentChatRoom.userIds)  // 멤버들한테 다 보내야함. 이부분은 단체 채팅부분에서 수정할것. 이게 아니라 챗 룸의 멤버아이디..로 서칭. 여기서 단체로 서칭.
            .get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    task.result?.documents?.let { documentSnapshots ->
                        for (documentSnapshot in
                        documentSnapshots.filter { it[UserModel.KEY_ID] != currentUserId }) {
                            otherUserTokens.add(documentSnapshot?.data?.get(KEY_PUSH_TOKEN) as String)
                        }

                        if (otherUserTokens.count() > 1) {
                            // 다수의 유저 케이스.
                            // 여기서 그토큰얻어오는 로직도 필요함. 응 꺼져.
                        } else {
                            sendSingleCloudMessage(otherUserTokens[0])
                            println("$TAG: publisher id found")
                        }

                        firstMessage = false
                    } ?: run {
                        showToast(requireContext(), getString(R.string.failed_to_find_publisher))
                        return@addOnCompleteListener
                    }
                } else {
                    showToast(requireContext(), getString(R.string.failed_to_find_publisher))
                    println("$TAG: ${task.exception}")
                }
            }
    }

    private fun sendCloudMessage(pushToken: String) { // 이 함수, 통일할 것.
        val url = "https://fcm.googleapis.com/fcm/send"
        val cloudMessage = CloudMessageModel()

        cloudMessage.to = pushToken

        // cloudMessage.notification.click_action = ACTION_MAIN
        cloudMessage.notification.title = currentUserPublicName
        cloudMessage.notification.text = edit_text_message.text.toString()

        cloudMessage.data.message = edit_text_message.text.toString()
        cloudMessage.data.roomId = chatRoom!!.roomId
        cloudMessage.data.senderPublicName = currentUserPublicName

        val requestBody =
            RequestBody.create(MediaType.parse("application/json; charset=utf8"),
                Gson().toJson(cloudMessage))
        val request = Request.Builder().header("Content-Type", "application/json")
            .addHeader("Authorization", API_KEY)
            .url(url)
            .post(requestBody)
            .build()

        val okHttpClient = OkHttpClient()
        okHttpClient.newCall(request).enqueue(object: Callback {
            override fun onFailure(request: Request?, e: IOException?) {
                showToast(requireContext(), getString(R.string.chat_message_sending_failure_message))
                println("$TAG: ${e?.message}")
            }

            override fun onResponse(response: Response?) {
                if (response?.isSuccessful == true)
                    println("$TAG: ${response.body()?.string()}")
                else {
                    showToast(requireContext(), getString(R.string.chat_message_sending_failure_message))
                    println("$TAG: message sending failed")
                }
            }
        })
    }

     */

    private fun generateChatMessage(message: String, time: String) =
        ChatMessageModel(
            message = message,
            readUsers = mutableListOf(userInformation.uid),
            senderId = userInformation.uid,
            time = time
        )

    private fun generateChatRoom(
        chatMessages: MutableList<ChatMessageModel>, creationTime: String,
        lastMessage: String, roomId: String, userIds: MutableList<String>
    ) =
        ChatRoomModel(
            chatMessages = chatMessages,
            creationTime = creationTime,
            lastMessage = lastMessage,
            lastMessageTime = creationTime,
            roomId = roomId,
            userIds = userIds
        )

    private fun getChatRoom(map: Map<*, *>) =
        gson.fromJson(JSONObject(map).toString(), ChatRoomModel::class.java)

    private fun sendMessage(message: String) {
        button_send.isEnabled = false
        edit_text_chat_message.text.clear()
        val newChatMessage = generateChatMessage(message, getCurrentTime())
        val newPosition = currentChatRoom.chatMessages.count()

        previousChatMessages = currentChatRoom.chatMessages.map { it.copy() }.toMutableList()
        currentChatRoom.chatMessages.add(newPosition, newChatMessage)

        chatRoomDocumentReference
            .update(mapOf(KEY_CHAT_MESSAGES to currentChatRoom.chatMessages))
            .addOnSuccessListener {
                button_send.isEnabled = true

            }
            .addOnFailureListener {
                (requireActivity() as MainActivity).errorHandler
                    .errorHandling(it, getString(R.string.failed_to_send_message))
                button_send.isEnabled = true
            }
    }

    inner class ChatMessageRecyclerViewAdapter(layoutId: Int = R.layout.item_view_chat):
        BaseRecyclerViewAdapter<ChatMessageModel>(layoutId) {

        init {
            setChatMessageSnapshotListener()
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val chatMessage = items[position]
            // setImage(holder.view.image_view_profile, chatMessage.profileUri)

            holder.view.text_view_message.text = chatMessage.message
        }

        private fun setChatMessageSnapshotListener() {
            chatRoomDocumentReference = chatRoomsCollectionReference.document(currentChatRoom.roomId)
            listenerRegistration = chatRoomDocumentReference.addSnapshotListener { snapshot, e ->
                if (e != null)
                    (requireActivity() as MainActivity).errorHandler
                        .errorHandling(e, getString(R.string.failed_to_load_chat_messages))
                else {
                    if (snapshot != null && snapshot.data != null) {
                        if(!chatMessagesIsInitialized) {
                            previousChatMessages = currentChatRoom.chatMessages.map { it.copy() }.toMutableList()
                            chatMessagesRecyclerViewAdapter.items = (currentChatRoom.chatMessages as ArrayList)
                            CoroutineScope(Dispatchers.Main).launch {
                                notifyDataSetChanged()
                                delay(400)
                                try {
                                    recyclerView.scrollToPosition(items.count() - 1)
                                } catch (e: UninitializedPropertyAccessException) {
                                    e.printStackTrace()
                                }
                            }
                            chatMessagesIsInitialized = !chatMessagesIsInitialized
                            return@addSnapshotListener
                        }

                        val receivedChatMessages = getChatMessages(snapshot.data!!)
                        val newMessages = receivedChatMessages - previousChatMessages
                        previousChatMessages = receivedChatMessages.map { it.copy() }.toMutableList()
                        if (newMessages.isNotEmpty()) {
                            chatMessagesRecyclerViewAdapter.insertAll(
                                items.count(),
                                messages = newMessages
                            )
                        }
                    }
                }
            }
        }

        private fun insertAll(position: Int = 0, messages: List<ChatMessageModel>) {
            items.addAll(position, messages.filter { it.senderId != userInformation.uid })
            CoroutineScope(Dispatchers.Main).launch {
                notifyDataSetChanged()
                delay(200)
                recyclerView.scrollToPosition(items.count() - 1)
            }
        }

        private fun getChatMessages(map: Map<*, *>)  =
            gson.fromJson(JSONObject(map).toString(), ChatRoomModel::class.java).chatMessages
    }
}