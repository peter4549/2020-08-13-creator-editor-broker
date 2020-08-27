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
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.android.synthetic.main.fragment_single_recycler_view.view.*
import kotlinx.android.synthetic.main.item_view_chat_room.view.*

class ChatRoomsFragment: Fragment() {

    private lateinit var listenerRegistration: ListenerRegistration
    private lateinit var userCollectionReference: CollectionReference
    private lateinit var usersInformationList: ArrayList<ArrayList<UserInformationModel>>
    private lateinit var chatRoomsRecyclerViewAdapter: ChatRoomsRecyclerViewAdapter

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

    private fun addUsersInformationAndUpdateUi(chatRoomUserIds: MutableList<String>, view: View) {
        userCollectionReference
            .whereArrayContainsAny(UserInformationModel.KEY_UID, chatRoomUserIds)
            .get()
            .addOnSuccessListener { documents ->
                val chatRoomUsers = arrayListOf<UserInformationModel>()
                for (document in documents) {
                    chatRoomUsers.add(document.toObject(UserInformationModel::class.java))
                }
                setImage(view.image_view_profile, chatRoomUsers[0].profileImageUri)
                view.text_view_users_name.text = chatRoomUsers.joinToString { it.publicName }
                usersInformationList.add(chatRoomUsers)
            }
            .addOnFailureListener { exception ->
                (requireActivity() as MainActivity).errorHandler
                    .errorHandling(exception, getString(R.string.failed_to_read_user_data))
            }
    }

    fun removeListenerRegistration() {
        if (::listenerRegistration.isInitialized)
            listenerRegistration.remove()
    }

    inner class ChatRoomsRecyclerViewAdapter:
        BaseRecyclerViewAdapter<ChatRoomModel>(layoutId = R.layout.item_view_chat_room) {

        init {
            setChatRoomSnapshotListener()
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val chatRoom = items[position]
            holder.view.text_view_last_message.text = chatRoom.lastMessage
            holder.view.text_view_time.text = chatRoom.lastMessageTime
            addUsersInformationAndUpdateUi(chatRoom.userIds, holder.view)
        }

        fun setChatRoomSnapshotListener() {
            listenerRegistration = FirebaseFirestore.getInstance()
                .collection(COLLECTION_CHAT_ROOMS)
                .whereEqualTo(ChatRoomModel.KEY_ROOM_ID,
                    MainActivity.currentUserInformation!!.chatRoomIds)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        showToast(requireContext(), getString(R.string.failed_to_load_chat_rooms))
                        error.printStackTrace()
                    } else {
                        for (change in value!!.documentChanges) {
                            when (change.type) {
                                DocumentChange.Type.ADDED ->
                                    chatRoomsRecyclerViewAdapter.insert(change.document.toObject(ChatRoomModel::class.java))
                                DocumentChange.Type.MODIFIED ->
                                    chatRoomsRecyclerViewAdapter.update(change.document.toObject(ChatRoomModel::class.java))
                                DocumentChange.Type.REMOVED ->
                                    chatRoomsRecyclerViewAdapter.remove(change.document.toObject(ChatRoomModel::class.java))
                            }
                        }
                        recyclerView.scheduleLayoutAnimation()
                        notifyDataSetChanged()
                    }
                }
        }
    }
}