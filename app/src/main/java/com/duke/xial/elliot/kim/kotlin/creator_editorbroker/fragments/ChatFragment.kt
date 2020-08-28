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
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.constants.FireStore.COLLECTION_USERS
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.ChatMessageModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.ChatRoomModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.ChatRoomModel.Companion.KEY_CHAT_MESSAGES
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.ChatRoomModel.Companion.KEY_USER_IDS
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.UserInformationModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.retrofit2.CloudMessaging
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.getCurrentTime
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.hashString
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.setImage
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.showToast
import com.google.firebase.firestore.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.fragment_chat.view.*
import kotlinx.android.synthetic.main.item_view_chat_left.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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

    private fun errorHandling(e: Exception, toastMessage: String, throwing: Boolean = false) {
        (requireActivity() as MainActivity).errorHandler.errorHandling(e, toastMessage, throwing)
    }

    private fun createChatRoom(message: String, view: View) {
        val creationTime = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
        val roomId = hashString(userInformation.uid + targetUser?.uid + creationTime).chunked(32)[0]
        val firstChatMessage = generateChatMessage(message, creationTime)
        currentChatRoom = generateChatRoom(
            mutableListOf(firstChatMessage),
            creationTime, message, roomId, mutableListOf(userInformation.uid, targetUser?.uid!!),
            mutableListOf(userInformation, targetUser)
        )

        MainActivity.currentUserInformation?.chatRoomIds?.add(roomId)

        chatRoomsCollectionReference
            .document(roomId)
            .set(currentChatRoom)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    initializeRecyclerView(view)
                    if (targetUser.pushToken != null)
                        sendCloudMessage(mutableListOf(targetUser.pushToken!!), message)
                    chatRoomCreated = true
                    MainActivity.currentChatRoomId = currentChatRoom.roomId
                } else
                    errorHandling(task.exception!!, getString(R.string.failed_to_create_chat_room))
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
    */

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

    private fun generateChatMessage(message: String, time: String) =
        ChatMessageModel(
            message = message,
            readUsers = mutableListOf(userInformation.uid),
            senderId = userInformation.uid,
            time = time
        )

    private fun generateChatRoom(
        chatMessages: MutableList<ChatMessageModel>, creationTime: String,
        lastMessage: String, roomId: String, userIds: MutableList<String>,
        users: MutableList<UserInformationModel>
    ) =
        ChatRoomModel(
            chatMessages = chatMessages,
            creationTime = creationTime,
            lastMessage = lastMessage,
            lastMessageTime = creationTime,
            roomId = roomId,
            userIds = userIds,
            users = users
        )

    private fun getChatRoom(map: Map<*, *>) =
        gson.fromJson(JSONObject(map).toString(), ChatRoomModel::class.java)

    private fun sendMessage(message: String) {
        button_send.isEnabled = false
        val newChatMessage = generateChatMessage(message, getCurrentTime())
        val newPosition = currentChatRoom.chatMessages.count()

        previousChatMessages = currentChatRoom.chatMessages.map { it.copy() }.toMutableList()
        currentChatRoom.chatMessages.add(newPosition, newChatMessage)

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

            if (chatMessage.senderId == userInformation.uid) {
                holder.view.text_view_message.text = chatMessage.message
                holder.view.text_view_time.text = chatMessage.time
            } else {
                setImage(holder.view.image_view_profile, userNameProfileUriMap[chatMessage.senderId]?.second)
                holder.view.text_view_public_name.text = userNameProfileUriMap[chatMessage.senderId]?.first
                holder.view.text_view_message.text = chatMessage.message
                holder.view.text_view_time.text = chatMessage.time
            }
        }

        private fun setChatRoomUsersInformation() {
            for (user in currentChatRoom.users) {
                userNameProfileUriMap[user.uid] = Pair(user.publicName, user.profileImageUri)
            }
        }

        private fun setChatMessageSnapshotListener() {
            chatRoomDocumentReference = chatRoomsCollectionReference.document(currentChatRoom.roomId)
            listenerRegistration = chatRoomDocumentReference.addSnapshotListener { snapshot, e ->
                if (e != null)
                    errorHandling(e, getString(R.string.failed_to_load_chat_messages))
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
                notifyItemRangeChanged(position, items.count())
                delay(200)
                recyclerView.scrollToPosition(items.count() - 1)
            }
        }

        private fun getChatMessages(map: Map<*, *>)  =
            gson.fromJson(JSONObject(map).toString(), ChatRoomModel::class.java).chatMessages
    }

    companion object {
        private const val TAG = "ChatFragment"
    }
}