package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.profiles

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.activities.MainActivity
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.activities.MainActivity.Companion.errorHandler
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.adapters.BaseRecyclerViewAdapter
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.adapters.GridLayoutManagerWrapper
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.constants.FireStore.COLLECTION_PR_LIST
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.constants.FireStore.COLLECTION_USERS
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.constants.REQUEST_CODE_YOUTUBE_CHANNELS
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.fragments.WritePrFragment
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.PrModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.UserModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.VideoDataModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.scaleDown
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.scaleUp
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.setImage
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.youtube.YouTubeChannelsActivity
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.youtube.toVideoData
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import kotlinx.android.synthetic.main.fragment_profile.view.fab_chat
import kotlinx.android.synthetic.main.fragment_profile.view.fab_eject
import kotlinx.android.synthetic.main.fragment_profile.view.text_view_public_name
import kotlinx.android.synthetic.main.item_view_image.view.*

class ProfileFragment(private val user: UserModel? = null): Fragment() {

    private var isFabOpen = false
    private val fabClickListener = View.OnClickListener { view ->
        when(view.id) {
            R.id.fab_eject -> {
                fab_eject.isMovable = isFabOpen
                animateFab()
            }
            R.id.fab_chat -> {
                (requireActivity() as MainActivity).startChatFragment(user!!)
            }
            R.id.fab_star -> {
                if (!MainActivity.currentUser?.userIdsReceivedMyStar?.contains(user?.uid)!!)
                    giveStar()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        initializeToolbar(view.toolbar)
        initializeUi(view)
        initializeImageRecyclerView(view.recycler_view_works)
        initializeFab(view)

        return view
    }

    private fun initializeToolbar(toolbar: Toolbar) {
        (requireActivity() as MainActivity).setSupportActionBar(toolbar)
        (requireActivity() as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)
    }

    private fun initializeUi(view: View) {
        setImage(view.image_view_profile, user?.profileImageUri)
        view.text_view_stars.text = user?.userIdsGaveMeStar?.count().toString()
        view.text_view_public_name.text = user?.publicName
        view.text_view_phone_number.text = user?.contactPhoneNumber ?: ""
        view.text_view_email.text = user?.contactPhoneNumber ?: ""
    }

    private fun initializeImageRecyclerView(recyclerView: RecyclerView) {
        recyclerView.apply {
            adapter = ImageRecyclerViewAdapter(user?.youtubeVideos?.map { it.toVideoData() } as ArrayList
                , R.layout.item_view_image)
            this.layoutManager = GridLayoutManagerWrapper(requireContext(), 1).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }
        }
    }

    private fun initializeFab(view: View) {
        view.fab_chat.setOnClickListener(fabClickListener)
        view.fab_eject.setOnClickListener(fabClickListener)
        view.fab_star.setOnClickListener(fabClickListener)
        view.fab_chat.isMovable = false
        view.fab_star.isMovable = false
        view.fab_chat.scaleDown()
        view.fab_star.scaleDown()
        view.fab_eject.setChildButtons(view.fab_chat, view.fab_star)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> requireActivity().onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun animateFab() {
        if (isFabOpen) {
            fab_eject.animate().rotation(0F).setDuration(240L).start()
            fab_chat.scaleDown()
            fab_star.scaleDown()
            fab_chat.isEnabled = false
            fab_star.isEnabled = false
        } else {
            fab_eject.animate().rotation(45F).setDuration(240L).start()
            fab_chat.scaleUp()
            fab_star.scaleUp()
            fab_chat.isEnabled = true
            fab_star.isEnabled = true
        }

        isFabOpen = !isFabOpen
    }

    private fun giveStar() {
        FirebaseFirestore.getInstance().collection(COLLECTION_USERS)
            .document(user?.uid!!).update(
                UserModel.KEY_USER_IDS_GAVE_STAR,
                FieldValue.arrayUnion(MainActivity.currentUser?.uid!!))
            .addOnSuccessListener {
                text_view_stars.text = (text_view_stars.text.toString().toInt() + 1).toString()
                MainActivity.currentUser?.userIdsReceivedMyStar?.add(user.uid)
                updatePr(MainActivity.currentUser!!)
            }
            .addOnFailureListener {
                errorHandler.errorHandling(it)
            }
    }

    private fun updatePr(user: UserModel) {
        FirebaseFirestore.getInstance().collection(COLLECTION_PR_LIST)
            .document(user.uid)
            .update(mapOf(PrModel.KEY_PUBLISHER to user))
            .addOnSuccessListener {
                println("$TAG: user updated")
            }
            .addOnFailureListener {
                errorHandler.errorHandling(it)
            }
    }

    inner class ImageRecyclerViewAdapter(val videos: ArrayList<VideoDataModel>,
                                         layoutId: Int = R.layout.item_view_image)
        : BaseRecyclerViewAdapter<VideoDataModel>(layoutId, videos) {

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            super.onBindViewHolder(holder, position)

            val video = videos[position]

            setImage(holder.view.image_view_thumbnail, video.thumbnailUri)
            holder.view.image_view_thumbnail.setOnClickListener {
                startYouTubePlayerActivity()
            }
        }

        private fun startYouTubePlayerActivity() {
            val intent = Intent(requireActivity(), YouTubeChannelsActivity::class.java)

            intent.action = WritePrFragment.ACTION_FROM_WRITING_FRAGMENT
            intent.putExtra(WritePrFragment.KEY_CHANNELS, MainActivity.currentUser?.channelIds?.toTypedArray())
            startActivityForResult(intent, REQUEST_CODE_YOUTUBE_CHANNELS)
        }
    }

    companion object {
        private const val TAG = "ProfileFragment"
    }
}