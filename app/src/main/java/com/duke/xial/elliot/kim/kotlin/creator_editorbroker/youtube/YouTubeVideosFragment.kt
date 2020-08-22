package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.youtube

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.error_handler.ResponseFailureException
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.PlaylistItemsModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.VideoModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.VideosItemsModel
import kotlinx.android.synthetic.main.fragment_youtube_playlists.view.toolbar
import kotlinx.android.synthetic.main.fragment_youtube_videos.view.*
import kotlinx.android.synthetic.main.item_view_video.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class YouTubeVideosFragment: Fragment() {

    private var nextPageToken: String? = null
    private var playlistId: String? = null

    fun setPlaylistId(playlistId: String) {
        this.playlistId = playlistId
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_youtube_videos, container, false)

        (requireActivity() as YouTubeChannelsActivity).setSupportActionBar(view.toolbar)
        (requireActivity() as YouTubeChannelsActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)

        view.recycler_view_videos.apply {
            adapter = VideosRecyclerView()
            layoutManager = GridLayoutManagerWrapper(requireContext(), 1)
        }

        return view
    }

    private inner class VideosRecyclerView(layoutId: Int = R.layout.item_view_video):
        BaseRecyclerViewAdapter<VideoModel>(layoutId = layoutId) {

        init {
            if ((requireActivity() as YouTubeChannelsActivity).youtubeDataManager
                    .videosInUse.keys.contains(playlistId)) {
                nextPageToken = (requireActivity() as YouTubeChannelsActivity).youtubeDataManager
                    .nextPageTokenOfVideos[playlistId]
                items = (requireActivity() as YouTubeChannelsActivity).youtubeDataManager
                    .videosInUse[playlistId] ?: arrayListOf()
                println("OOOOOOOOOOOOOOO")
            } else
                playlistId?.let { setPlaylistItemsById(it) }
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val video = items[position]
            loadImage(holder.view.image_view_thumbnail, video.snippet.thumbnails.standard?.url
                ?: video.snippet.thumbnails.medium.url)
            holder.view.text_view_title.text = video.snippet.title
            holder.view.text_view_view_count.text = video.statistics.viewCount
            holder.view.text_view_published_time.text = video.snippet.publishedAt
            holder.view.setOnClickListener {
                VideoClickDialogFragment().show(requireFragmentManager(), tag)
            }
        }

        private fun setPlaylistItemsById(playlistId: String) {
            (requireActivity() as YouTubeChannelsActivity).youTubeDataApi.getPlaylistItemsService()
                .requestById(playlistId = playlistId)
                .enqueue(object : Callback<PlaylistItemsModel> {
                    override fun onFailure(call: Call<PlaylistItemsModel>, t: Throwable) {
                        (requireActivity() as YouTubeChannelsActivity).errorHandler
                            .errorHandling(t, getString(R.string.failed_to_get_playlists))
                    }

                    override fun onResponse(
                        call: Call<PlaylistItemsModel>,
                        response: Response<PlaylistItemsModel>
                    ) {
                        val playlistItems = response.body()
                        if (playlistItems != null) {
                            val nextPageToken = playlistItems.nextPageToken
                            val joinedVideoIds =
                                playlistItems.items.joinToString(",") { it.contentDetails.videoId }
                            setVideosByIds(nextPageToken, joinedVideoIds)
                        } else
                            (requireActivity() as YouTubeChannelsActivity).errorHandler
                                .errorHandling(
                                    ResponseFailureException("failed to get playlist items",
                                    response.errorBody()!!)
                                )
                    }
                })
        }

        private fun setVideosByIds(nextPageToken: String?, videoIds: String) {
            (requireActivity() as YouTubeChannelsActivity).youTubeDataApi.getVideosService()
                .requestById(id = videoIds)
                .enqueue(object : Callback<VideosItemsModel>{
                    override fun onFailure(call: Call<VideosItemsModel>, t: Throwable) {
                        (requireActivity() as YouTubeChannelsActivity).errorHandler
                            .errorHandling(t, getString(R.string.failed_to_get_videos))
                    }

                    override fun onResponse(
                        call: Call<VideosItemsModel>,
                        response: Response<VideosItemsModel>
                    ) {
                        val videoItems = response.body()
                        if (videoItems != null) {
                            items = videoItems.items as ArrayList<VideoModel>
                            CoroutineScope(Dispatchers.Main).launch {
                                recyclerView.scheduleLayoutAnimation()
                                notifyDataSetChanged()
                            }
                            (requireActivity() as YouTubeChannelsActivity).youtubeDataManager
                                .registerNextPageTokenAndVideos(playlistId!!, nextPageToken, items)
                        } else {
                            (requireActivity() as YouTubeChannelsActivity).errorHandler
                                .errorHandling(ResponseFailureException("failed to get videos",
                                    response.errorBody()!!))
                        }
                    }
                })
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