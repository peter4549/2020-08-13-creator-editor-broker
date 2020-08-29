package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.activities.MainActivity
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.adapters.BaseRecyclerViewAdapter
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.adapters.GridLayoutManagerWrapper
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.adapters.SpinnerAdapter
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.constants.FireStore.COLLECTION_PR_LIST
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.constants.PR_LIST
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.constants.REQUEST_CODE_YOUTUBE_CHANNELS
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.PrModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.VideoDataModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.getCurrentTime
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.hashString
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.showToast
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.youtube.YouTubeChannelsActivity
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.youtube.YouTubeChannelsActivity.Companion.KEY_VIDEO_DATA
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_write_pr.*
import kotlinx.android.synthetic.main.fragment_write_pr.view.*
import kotlinx.android.synthetic.main.fragment_write_pr.view.button_register
import kotlinx.android.synthetic.main.item_view_image.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WritePrFragment: Fragment() {

    private lateinit var imageRecyclerViewAdapter: ImageRecyclerViewAdapter
    private lateinit var targets: Array<String>
    private var selectedImageViewPosition = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_write_pr, container, false)

        initializeSpinner(view.spinner_target)
        initializeImageRecyclerView(view.recycler_view)

        view.button_register.setOnClickListener {
            registerPr()
        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_YOUTUBE_CHANNELS -> {
                    if (data != null) {
                        val video = data.getSerializableExtra(KEY_VIDEO_DATA) as VideoDataModel
                        imageRecyclerViewAdapter.update(selectedImageViewPosition, video)
                    }
                }
            }
        }
    }

    private fun initializeSpinner(spinner: Spinner) {
        targets = arrayOf(
            "-",
            getString(R.string.find_creator),
            getString(R.string.find_editor)
        )
        spinner.adapter = SpinnerAdapter(requireContext(), targets)
    }

    private fun initializeImageRecyclerView(recyclerView: RecyclerView) {
        imageRecyclerViewAdapter = ImageRecyclerViewAdapter(arrayListOf(null), R.layout.item_view_image)
        recyclerView.apply {
            adapter = imageRecyclerViewAdapter
            this.layoutManager = GridLayoutManagerWrapper(requireContext(), 1).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }
        }
    }

    private fun clearUi() {

    }


    /*data class PrModel(var categories: MutableList<String?>,
                       var description: String,
                       var userType: String,
                       var publisherId: String,
                       var publisherPublicName: String,
                       var registrationTime: String,
                       var tier: Int,
                       var title: String,
                       var youtubeVideos: MutableList<VideoDataModel> = mutableListOf())

     */

    private fun registerPr() {
        val title = edit_text_title.text.toString()
        val description = edit_text_description.text.toString()
        val target = spinner_target.selectedItemPosition

        when {
            title.isBlank() -> {
                showToast(requireContext(), getString(R.string.please_enter_title))
                return
            }
            description.isBlank() -> {
                showToast(requireContext(), getString(R.string.please_enter_description))
                return
            }
            target == 0 -> {
                showToast(requireContext(), getString(R.string.please_select_target))
                return
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            val userInformation = MainActivity.currentUserInformation!!
            val id = hashString(MainActivity.currentUserInformation?.uid!! +
                    getCurrentTime().toString()).chunked(32)[0]
            val pr = PrModel(
                categories = userInformation.categories,
                description = description,
                id = id,
                publisherId = userInformation.uid,
                publisherPublicName = userInformation.publicName,
                registrationTime = getCurrentTime(),
                target = target,
                tier = userInformation.tier,
                title = title,
                youtubeVideos = imageRecyclerViewAdapter.videos.toMutableList(),
                userInformation = userInformation
            )

            if (userInformation.myPrIds.count() < 1)
                setPrToFireStore(pr)
            else
                showToast(requireContext(), "기본회원은 1개만 가능.")
                // updatePrToFireStore(pr)
        }
    }

    private fun setPrToFireStore(pr: PrModel) {
        FirebaseFirestore.getInstance()
            .collection(COLLECTION_PR_LIST)
            .document(pr.id)
            .set(pr)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast(requireContext(), "PR이 등록되었습니다.1")
                    clearUi()
                    MainActivity.currentUserInformation!!.myPrIds.add(pr.id)
                    MainActivity.ChangedData.prListChanged = true
                    //button_upload.isEnabled = true
                }
                else {
                    (requireActivity() as MainActivity).errorHandler
                        .errorHandling(task.exception!!, "PR을 등록하지 못했습니다.")
                    //button_upload.isEnabled = true
                }
            }
    }

    private fun updatePrToFireStore(pr: PrModel) {
        FirebaseFirestore.getInstance()
            .collection(COLLECTION_PR_LIST)
            .document(pr.id)
            .update(PR_LIST, FieldValue.arrayUnion(pr))
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast(requireContext(), getString(R.string.pr_has_registered))
                    clearUi()
                    MainActivity.currentUserInformation!!.myPrIds.add(pr.id)
                    MainActivity.ChangedData.prListChanged = true
                    //button_upload.isEnabled = true
                }
                else {
                    (requireActivity() as MainActivity).errorHandler
                        .errorHandling(task.exception!!, getString(R.string.failed_to_register_pr))
                    //button_upload.isEnabled = true
                }
            }
    }

    inner class ImageRecyclerViewAdapter(val videos: ArrayList<VideoDataModel?>,
                                         layoutId: Int = R.layout.item_view_image)
        : BaseRecyclerViewAdapter<VideoDataModel?>(layoutId, videos) {

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            super.onBindViewHolder(holder, position)
            val video = videos[position]
            if (video != null)
                setThumbnail(holder.view.image_view_thumbnail, video.thumbnailUri)

            holder.view.image_view_thumbnail.setOnClickListener {
                selectedImageViewPosition = position
                // selectedImageView = it as ImageView
                // VideoOptionDialogFragment().show(requireFragmentManager(), TAG)
                startYouTubeChannelsActivity()
            }
        }

        private fun setThumbnail(imageView: ImageView, uri: String?) {
            if (uri != null)
                Glide.with(imageView.context)
                    .load(uri)
                    .placeholder(R.drawable.ic_round_add_to_photos_80)
                    .error(R.drawable.ic_baseline_sentiment_dissatisfied_80)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .transform(CenterCrop(), RoundedCorners(8))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .listener(null)
                    .into(imageView)
        }

        private fun startYouTubeChannelsActivity() {
            val intent = Intent(requireActivity(), YouTubeChannelsActivity::class.java)

            intent.action = ACTION_FROM_WRITING_FRAGMENT
            intent.putExtra(KEY_CHANNELS, MainActivity.currentUserInformation?.channelIds?.toTypedArray())
            startActivityForResult(intent, REQUEST_CODE_YOUTUBE_CHANNELS)
        }

        fun update(position: Int, item: VideoDataModel) {
            super.update(position, item)
            insert(null, position + 1)
            recyclerView.scheduleLayoutAnimation()
            notifyItemInserted(position + 1)
        }
    }

    companion object {
        const val ACTION_FROM_WRITING_FRAGMENT = "write.pr.fragment.action.from.writing.fragment"
        const val KEY_CHANNELS = "key_channel"
    }
}