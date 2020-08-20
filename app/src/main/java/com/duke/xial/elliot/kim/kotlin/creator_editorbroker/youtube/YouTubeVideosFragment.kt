package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.youtube

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.adapters.GridLayoutManagerWrapper
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.VideoModel
import kotlinx.android.synthetic.main.fragment_youtube_playlists.view.*

class YouTubeVideosFragment: Fragment() {
    private var videos: ArrayList<VideoModel>? = null
    private var nextPageToken: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_youtube_videos, container, false)

        (requireActivity() as YouTubeChannelsActivity).setSupportActionBar(view.toolbar)
        (requireActivity() as YouTubeChannelsActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)

        /*
        view.recycler_view_playlists.apply {
            adapter = PlaylistsRecyclerViewAdapter(playlists ?: arrayListOf())
            layoutManager = GridLayoutManagerWrapper(requireContext(), 1)
        }

         */
        return view
    }

    private inner class VideosRecyclerView(private val videos: ArrayList<VideoModel>,
                                           layoutId: Int = R.layout.item_view_playlist)
}