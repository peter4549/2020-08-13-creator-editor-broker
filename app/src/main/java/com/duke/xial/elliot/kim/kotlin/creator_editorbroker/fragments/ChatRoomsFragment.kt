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
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.constants.FireStore.COLLECTION_USERS
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.ChatRoomModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.UserInformationModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.setImage
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.showToast
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.toLocalTimeString
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_single_recycler_view.view.*
import kotlinx.android.synthetic.main.item_view_chat_room.view.*
import org.json.JSONObject

class ChatRoomsFragment: Fragment() {

    private lateinit var chatRoomsListenerRegistration: ListenerRegistration
    private lateinit var userCollectionReference: CollectionReference
    private val usersInformationList: ArrayList<ArrayList<UserInformationModel>> = arrayListOf()
    private lateinit var chatRoomsRecyclerViewAdapter: ChatRoomsRecyclerViewAdapter
    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_single_recycler_view, container, false)
        userCollectionReference = FirebaseFirestore.getInstance().collection(COLLECTION_USERS)
        chatRoomsRecyclerViewAdapter = ChatRoomsRecyclerViewAdapter()
        view.recycler_view.apply {
            adapter = chatRoomsRecyclerViewAdapter
            layoutManager = GridLayoutManagerWrapper(requireContext(), 1)
        }
        return view
    }

    fun removeListenerRegistration() {
        if (::chatRoomsListenerRegistration.isInitialized)
            chatRoomsListenerRegistration.remove()
    }

    inner class ChatRoomsRecyclerViewAdapter:
        BaseRecyclerViewAdapter<ChatRoomModel>(layoutId = R.layout.item_view_chat_room) {

        init {
            setChatRoomSnapshotListener()
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val chatRoom = items[position]
            val profileImageUris =
                chatRoom.users.filter { it.uid != MainActivity.currentUserInformation?.uid }
                    .filter { it.profileImageUri != null && it.profileImageUri != "null" }
                    .map { it.profileImageUri }
            if (profileImageUris.isNotEmpty())
                setImage(holder.view.image_view_profile, profileImageUris[0])  // 이후 반복문으로 전환할 것.
            holder.view.text_view_users_name.text = chatRoom.users.joinToString { it.publicName }
            holder.view.text_view_last_message.text = chatRoom.lastMessage.message
            holder.view.text_view_time.text = chatRoom.lastMessage.time.toLocalTimeString()
        }

        private fun setChatRoomSnapshotListener() {
            chatRoomsListenerRegistration = FirebaseFirestore.getInstance()
                .collection(COLLECTION_CHAT_ROOMS)
                .whereArrayContains(ChatRoomModel.KEY_USER_IDS,
                    MainActivity.currentUserInformation!!.uid)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        showToast(requireContext(), getString(R.string.failed_to_load_chat_rooms))
                        error.printStackTrace()
                    } else {
                        for (change in value!!.documentChanges) {
                            when (change.type) {
                                DocumentChange.Type.ADDED ->
                                    chatRoomsRecyclerViewAdapter.insert(getChatRoom(change.document.data))
                                DocumentChange.Type.MODIFIED ->
                                    chatRoomsRecyclerViewAdapter.update(getChatRoom(change.document.data))
                                DocumentChange.Type.REMOVED ->
                                    chatRoomsRecyclerViewAdapter.remove(getChatRoom(change.document.data))
                            }
                        }
                        recyclerView.scheduleLayoutAnimation()
                        notifyDataSetChanged()
                    }
                }
        }
    }

    private fun getChatRoom(map: Map<String, Any>): ChatRoomModel {
        println("MMMMMMMM + " + map)
        return gson.fromJson(JSONObject(map).toString(), ChatRoomModel::class.java)
    }
}