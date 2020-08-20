package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.youtube

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.adapters.BaseRecyclerViewAdapter
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.adapters.GridLayoutManagerWrapper
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.PlaylistDataModel
import kotlinx.android.synthetic.main.fragment_youtube_playlists.*
import kotlinx.android.synthetic.main.fragment_youtube_playlists.view.*
import kotlinx.android.synthetic.main.item_view_playlist.view.*

class YouTubePlaylistsFragment: Fragment() {

    private var playlists: ArrayList<PlaylistDataModel>? = null
    private var nextPageToken: String? = null

    fun setNextToken(nextPageToken: String?) {
        this.nextPageToken = nextPageToken
    }

    fun setPlaylists(playlists: ArrayList<PlaylistDataModel>?) {
        this.playlists = playlists
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_youtube_playlists, container, false)

        (requireActivity() as YouTubeChannelsActivity).setSupportActionBar(view.toolbar)
        (requireActivity() as YouTubeChannelsActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)

        view.recycler_view_playlists.apply {
            adapter = PlaylistsRecyclerViewAdapter(playlists ?: arrayListOf())
            layoutManager = GridLayoutManagerWrapper(requireContext(), 1)
        }
        return view
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> requireActivity().onBackPressed()
        }

        return super.onOptionsItemSelected(item)
    }

    inner class PlaylistsRecyclerViewAdapter(private val playlists: ArrayList<PlaylistDataModel>,
                                             layoutId: Int = R.layout.item_view_playlist)
        : BaseRecyclerViewAdapter<PlaylistDataModel>(playlists, layoutId) {

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val playlist = playlists[position]

            loadImage(holder.view.image_view_thumbnail, playlist.thumbnailUri)
            holder.view.text_view_title.text = playlist.title
            holder.view.text_view_description.text = playlist.description
        }

        private fun loadImage(imageView: ImageView, imageUri: String) {
            Glide.with(imageView.context)
                .load(imageUri)
                .error(R.drawable.ic_baseline_sentiment_dissatisfied_80)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .transform(CenterCrop(), RoundedCorners(8))
                .transition(DrawableTransitionOptions.withCrossFade())
                .listener(null)
                .into(imageView)
        }
    }
}