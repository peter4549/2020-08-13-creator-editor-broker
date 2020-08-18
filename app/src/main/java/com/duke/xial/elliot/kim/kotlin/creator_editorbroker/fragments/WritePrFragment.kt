package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.constants.REQUEST_CODE_YOUTUBE_CHANNELS
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.youtube.YouTubeChannelsActivity
import kotlinx.android.synthetic.main.fragment_write_pr.view.*
import kotlinx.android.synthetic.main.item_view_image.view.*

class WritePrFragment: Fragment() {

    private var selectedChannelId: String? = null
    private var selectedPlaylistId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_write_pr, container, false)

        initializeImageRecyclerView(view.recycler_view)

        view.button_register.setOnClickListener {

        }

        return view
    }

    private fun initializeImageRecyclerView(recyclerView: RecyclerView) {
        recyclerView.apply {
            adapter = ImageRecyclerViewAdapter(arrayListOf(null), R.layout.item_view_image)
            this.layoutManager = GridLayoutManagerWrapper(requireContext(), 1).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }
        }
    }

    private fun clearAll() {

    }

    private fun registerPr() {

    }

    private fun createPr() {

    }

    inner class ImageRecyclerViewAdapter(private val thumbnailUris: ArrayList<Uri?>,
                                         layoutId: Int = R.layout.item_view_image)
        : BaseRecyclerViewAdapter<Uri?>(thumbnailUris, layoutId) {

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            super.onBindViewHolder(holder, position)
            setThumbnail(holder.view.image_view_thumbnail, thumbnailUris[position])
            holder.view.image_view_thumbnail.setOnClickListener {
                // selectedImageView = it as ImageView
                // VideoOptionDialogFragment().show(requireFragmentManager(), TAG)
                startYouTubeChannelsActivity() // 위에처럼 다이얼로그 추가할것. 아 아니라. 이미 등록된 애에 한해서.
            }
        }

        private fun setThumbnail(imageView: ImageView, uri: Uri?) {
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
    }

    companion object {
        const val ACTION_FROM_WRITING_FRAGMENT = "write.pr.fragment.action.from.writing.fragment"
        const val KEY_CHANNELS = "key_channel"
    }
}