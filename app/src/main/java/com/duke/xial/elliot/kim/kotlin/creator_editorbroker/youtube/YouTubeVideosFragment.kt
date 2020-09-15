package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.youtube

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.adapters.BaseRecyclerViewAdapter
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.adapters.GridLayoutManagerWrapper
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.PlaylistItemsModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.VideoDataModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.VideoModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.VideosItemsModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.*
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.toLocalTimeString
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.toMilliseconds
import kotlinx.android.synthetic.main.fragment_youtube_videos.view.*
import kotlinx.android.synthetic.main.fragment_youtube_videos.view.toolbar
import kotlinx.android.synthetic.main.item_view_video.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class YouTubeVideosFragment: Fragment() {

    private lateinit var errorText: TextView
    private lateinit var videosRecyclerViewAdapter: VideosRecyclerViewAdapter
    private var nextPageToken: String? = null
    private var playlistId: String? = null
    private val responseFailureCallback = {
        errorText.text = getString(R.string.failed_to_get_videos)
        errorText.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorError))
        errorText.visibility = View.VISIBLE
    }

    fun setPlaylistId(playlistId: String) {
        this.playlistId = playlistId
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_youtube_videos, container, false)

        (requireActivity() as YouTubeChannelsActivity).setSupportActionBar(view.toolbar)
        (requireActivity() as YouTubeChannelsActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)

        errorText = view.text_error

        videosRecyclerViewAdapter = VideosRecyclerViewAdapter()
        view.recycler_view_videos.apply {
            adapter = videosRecyclerViewAdapter
            layoutManager = GridLayoutManagerWrapper(requireContext(), 1)
            addOnScrollListener(object: RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    // if (!recyclerView.canScrollVertically(1)) {  }

                    val lastVisibleItemPosition =
                        (view.recycler_view_videos.layoutManager as GridLayoutManagerWrapper).findLastVisibleItemPosition()
                    if (videosRecyclerViewAdapter.itemCount > 19
                        && lastVisibleItemPosition >= (videosRecyclerViewAdapter.itemCount * 0.7).toInt()) {
                        if (nextPageToken != null) {
                            playlistId?.let { videosRecyclerViewAdapter.loadAdditionalVideos(it) }
                                ?: run {
                                    responseFailureCallback.invoke()
                                }
                        }
                    }
                }
            })
        }

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> requireActivity().onBackPressed()
        }

        return super.onOptionsItemSelected(item)
    }

    private inner class VideosRecyclerViewAdapter(layoutId: Int = R.layout.item_view_video):
        BaseRecyclerViewAdapter<VideoModel>(layoutId = layoutId) {

        init {
            if ((requireActivity() as YouTubeChannelsActivity).youtubeDataManager
                    .videosInUse.keys.contains(playlistId)
            ) {
                nextPageToken = (requireActivity() as YouTubeChannelsActivity).youtubeDataManager
                    .nextPageTokenOfVideos[playlistId]
                items = (requireActivity() as YouTubeChannelsActivity).youtubeDataManager
                    .videosInUse[playlistId] ?: arrayListOf()
                if (items.isEmpty())
                    errorText.visibility = View.VISIBLE
            } else
                playlistId?.let { setPlaylistItemsById(it) }
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val video = items[position]
            loadImage(
                holder.view.image_view_thumbnail, video.snippet.thumbnails.standard?.url
                    ?: video.snippet.thumbnails.default.url
            )
            holder.view.text_view_title.text = video.snippet.title
            holder.view.text_view_view_count.text = "%,d".format(video.statistics.viewCount.toInt())
            holder.view.text_view_published_time.text = video.snippet.publishedAt
                .extractNumbers().toLocalDateString()?.toMilliseconds()?.toLocalTimeString()
            holder.view.setOnClickListener {
                VideoClickDialogFragment(video.toVideoData()).show(requireFragmentManager(), tag)
            }
        }

        private fun setPlaylistItemsById(playlistId: String) {
            (requireActivity() as YouTubeChannelsActivity).youTubeDataApi.getPlaylistItemsService()
                .requestById(playlistId = playlistId)
                .enqueue(object : Callback<PlaylistItemsModel> {
                    override fun onFailure(call: Call<PlaylistItemsModel>, t: Throwable) {
                        showToast(requireContext(), getString(R.string.failed_to_get_playlists))
                        t.printStackTrace()
                        responseFailureCallback.invoke()
                    }

                    override fun onResponse(
                        call: Call<PlaylistItemsModel>,
                        response: Response<PlaylistItemsModel>
                    ) {
                        val playlistItems = response.body()
                        if (playlistItems != null) {
                            nextPageToken = playlistItems.nextPageToken
                            val joinedVideoIds =
                                playlistItems.items.joinToString(",") { it.contentDetails.videoId }
                            setVideosByIds(nextPageToken, joinedVideoIds)
                        } else
                            (requireActivity() as YouTubeChannelsActivity).youtubeExceptionHandler
                                .youtubeDataApiExceptionHandling(
                                    YouTubeExceptionHandler.YouTubeResponseFailureException(
                                        "failed to get playlist items",
                                        response.errorBody()!!
                                    ), null, responseFailureCallback
                                )
                    }
                })
        }

        private fun setVideosByIds(nextPageToken: String?, videoIds: String) {
            (requireActivity() as YouTubeChannelsActivity).youTubeDataApi.getVideosService()
                .requestById(id = videoIds)
                .enqueue(object : Callback<VideosItemsModel> {
                    override fun onFailure(call: Call<VideosItemsModel>, t: Throwable) {
                        showToast(requireContext(), getString(R.string.failed_to_get_videos))
                        t.printStackTrace()
                        responseFailureCallback.invoke()
                    }

                    override fun onResponse(
                        call: Call<VideosItemsModel>,
                        response: Response<VideosItemsModel>
                    ) {
                        val videoItems = response.body()
                        if (videoItems != null) {
                            items = videoItems.items as ArrayList<VideoModel>
                            if (items.isEmpty())
                                errorText.visibility = View.VISIBLE
                            CoroutineScope(Dispatchers.Main).launch {
                                recyclerView.scheduleLayoutAnimation()
                                notifyDataSetChanged()
                            }
                            (requireActivity() as YouTubeChannelsActivity).youtubeDataManager
                                .registerNextPageTokenAndVideos(playlistId!!, nextPageToken, items)
                        } else {
                            (requireActivity() as YouTubeChannelsActivity).youtubeExceptionHandler
                                .youtubeDataApiExceptionHandling(
                                    YouTubeExceptionHandler.YouTubeResponseFailureException(
                                        "failed to get videos",
                                        response.errorBody()!!
                                    ), null, responseFailureCallback
                                )
                        }
                    }
                })
        }

        fun loadAdditionalVideos(playlistId: String) {
            (requireActivity() as YouTubeChannelsActivity).youTubeDataApi.getPlaylistItemsService()
                .requestById(playlistId = playlistId, pageToken = nextPageToken ?: "")
                .enqueue(object : Callback<PlaylistItemsModel> {
                    override fun onFailure(call: Call<PlaylistItemsModel>, t: Throwable) {
                        showToast(requireContext(), getString(R.string.failed_to_get_playlists))
                        t.printStackTrace()
                        responseFailureCallback.invoke()
                    }

                    override fun onResponse(
                        call: Call<PlaylistItemsModel>,
                        response: Response<PlaylistItemsModel>
                    ) {
                        val playlistItems = response.body()
                        if (playlistItems != null) {
                            nextPageToken = playlistItems.nextPageToken
                            val joinedVideoIds =
                                playlistItems.items.joinToString(",") { it.contentDetails.videoId }
                            addVideosByIds(nextPageToken, joinedVideoIds)
                        } else
                            (requireActivity() as YouTubeChannelsActivity).youtubeExceptionHandler
                                .youtubeDataApiExceptionHandling(
                                    YouTubeExceptionHandler.YouTubeResponseFailureException(
                                        "failed to get playlist items",
                                        response.errorBody()!!
                                    ), null, responseFailureCallback
                                )
                    }
                })
        }

        private fun addVideosByIds(nextPageToken: String?, videoIds: String) {
            (requireActivity() as YouTubeChannelsActivity).youTubeDataApi.getVideosService()
                .requestById(id = videoIds)
                .enqueue(object : Callback<VideosItemsModel> {
                    override fun onFailure(call: Call<VideosItemsModel>, t: Throwable) {
                        showToast(requireContext(), getString(R.string.failed_to_get_videos))
                        t.printStackTrace()
                        responseFailureCallback.invoke()
                    }

                    override fun onResponse(
                        call: Call<VideosItemsModel>,
                        response: Response<VideosItemsModel>
                    ) {
                        val videoItems = response.body()
                        if (videoItems != null) {
                            val previousCount = items.count()
                            items.addAll(previousCount, videoItems.items as ArrayList<VideoModel>)
                            if (items.isEmpty())
                                errorText.visibility = View.VISIBLE
                            CoroutineScope(Dispatchers.Main).launch {
                                recyclerView.itemAnimator?.changeDuration = 0
                                videosRecyclerViewAdapter.notifyItemRangeInserted(previousCount, videoItems.items.count())
                            }
                            (requireActivity() as YouTubeChannelsActivity).youtubeDataManager
                                .registerNextPageTokenAndVideos(playlistId!!, nextPageToken, items)
                        } else {
                            (requireActivity() as YouTubeChannelsActivity).youtubeExceptionHandler
                                .youtubeDataApiExceptionHandling(
                                    YouTubeExceptionHandler.YouTubeResponseFailureException(
                                        "failed to get videos",
                                        response.errorBody()!!
                                    ), null, responseFailureCallback
                                )
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

fun VideoModel.toVideoData(): VideoDataModel {
    val snippet = this.snippet
    return VideoDataModel(
        channelId = snippet.channelId,
        description = snippet.description,
        id = this.id,
        publishedAt = snippet.publishedAt,
        thumbnailUri = snippet.thumbnails.standard?.url ?: snippet.thumbnails.default.url,
        title = snippet.title,
        viewCount = this.statistics.viewCount
    )
}