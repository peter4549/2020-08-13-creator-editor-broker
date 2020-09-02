package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.activities.MainActivity.Companion.categoriesMap
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.activities.MainActivity.Companion.errorHandler
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.activities.MainActivity.Companion.userTypesMap
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.adapters.BaseRecyclerViewAdapter
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.adapters.GridLayoutManagerWrapper
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.constants.FireStore.COLLECTION_USERS
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.UserModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.UserModel.Companion.KEY_REGISTERED_ON_PARTNERS
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.setImage
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_single_recycler_view.view.*
import kotlinx.android.synthetic.main.item_view_partner.view.*
import org.json.JSONObject

class PublicPartnersFragment: Fragment() {

    private lateinit var listenerRegistration: ListenerRegistration
    private lateinit var partnersCollectionReference: CollectionReference
    @Suppress("SpellCheckingInspection")
    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_single_recycler_view, container, false)
        view.recycler_view.apply {
            adapter = PartnersRecyclerViewAdapter()
            layoutManager = GridLayoutManagerWrapper(requireContext(), 1)
        }
        return view
    }

    inner class PartnersRecyclerViewAdapter:
        BaseRecyclerViewAdapter<UserModel>(layoutId = R.layout.item_view_partner) {

        private val partnersCollectionReference =
            FirebaseFirestore.getInstance().collection(COLLECTION_USERS)
                .whereEqualTo(KEY_REGISTERED_ON_PARTNERS, true)

        init {
            setPartnersSnapshotListener()
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val user  = items[position]

            setImage(holder.view.image_view_profile, user.profileImageUri)
            holder.view.text_view_public_name.text = user.publicName
            holder.view.text_view_category_01.text = categoriesMap[user.categories[0]]
            if (user.categories.count() > 1)
                holder.view.text_view_category_02.text = categoriesMap[user.categories[1]]

            holder.view.text_view_user_type.text = userTypesMap[user.userType]
            holder.view.text_view_stars.text = user.userIdsGaveMeStar.count().toString()
        }

        private fun setPartnersSnapshotListener() {
            listenerRegistration = partnersCollectionReference.addSnapshotListener { documentSnapshot, fireStoreException ->
                if (fireStoreException != null)
                    errorHandler.errorHandling(fireStoreException)
                else {
                    for (change in documentSnapshot!!.documentChanges) {
                        when (change.type) {
                            DocumentChange.Type.ADDED -> this.insert(change.document.data.toUserModel())
                            DocumentChange.Type.MODIFIED -> this.findUserAndUpdate(change.document.data.toUserModel())
                            DocumentChange.Type.REMOVED -> this.remove(change.document.data.toUserModel())
                            else -> { println("${PrListFragment.TAG}: unexpected DocumentChange Type") }
                        }
                    }
                }
            }
        }

        private fun findUserAndUpdate(user: UserModel) {
            val position = items.indexOf(items.find { it.uid == user.uid })

            if (position == -1)
                return

            items[position] = user
            notifyItemChanged(position)
        }
    }

    fun Map<*, *>.toUserModel(): UserModel =
        gson.fromJson(JSONObject(this).toString(), UserModel::class.java)
}

