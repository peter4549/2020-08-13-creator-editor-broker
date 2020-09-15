package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.youtube

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.Toast
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
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.ItemModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.PlaylistDataModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.PlaylistsModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.VideoModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.showToast
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.youtube.YouTubeChannelsActivity.Companion.TAG_YOUTUBE_VIDEOS_FRAGMENT
import kotlinx.android.synthetic.main.fragment_youtube_playlists.view.*
import kotlinx.android.synthetic.main.fragment_youtube_playlists.view.toolbar
import kotlinx.android.synthetic.main.item_view_playlist.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class YouTubePlaylistsFragment: Fragment() {

    private lateinit var playlistsRecyclerViewAdapter: PlaylistsRecyclerViewAdapter
    private var channelId: String? = null
    private var playlists: ArrayList<PlaylistDataModel>? = null
    private var nextPageToken: String? = null

    fun setChannelId(channelId: String?) {
        this.channelId = channelId
    }

    fun setNextPageToken(nextPageToken: String?) {
        this.nextPageToken = nextPageToken
    }

    fun setPlaylists(playlists: ArrayList<PlaylistDataModel>?) {
        this.playlists = playlists
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_youtube_playlists, container, false)

        (requireActivity() as YouTubeChannelsActivity).setSupportActionBar(view.toolbar)
        (requireActivity() as YouTubeChannelsActivity).supportActionBar?.setDisplayHomeAsUpEnabled(
            true
        )
        setHasOptionsMenu(true)

        playlistsRecyclerViewAdapter = PlaylistsRecyclerViewAdapter(playlists ?: arrayListOf())

        view.recycler_view_playlists.apply {
            adapter = playlistsRecyclerViewAdapter
            layoutManager = GridLayoutManagerWrapper(requireContext(), 1)
            addOnScrollListener(object: RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    // if (!recyclerView.canScrollVertically(1)) {  }

                    val lastVisibleItemPosition =
                        (view.recycler_view_playlists.layoutManager as GridLayoutManagerWrapper).findLastVisibleItemPosition()
                    if (playlistsRecyclerViewAdapter.itemCount > 9
                        && lastVisibleItemPosition >= (playlistsRecyclerViewAdapter.itemCount * 0.7).toInt()) {
                        if (nextPageToken != null) {
                            channelId?.let { playlistsRecyclerViewAdapter.addPlaylists(it) }
                                ?: run {
                                    showToast(requireContext(), getString(R.string.failed_to_get_playlists))
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

    inner class PlaylistsRecyclerViewAdapter(
        private val playlists: ArrayList<PlaylistDataModel>,
        layoutId: Int = R.layout.item_view_playlist
    )
        : BaseRecyclerViewAdapter<PlaylistDataModel>(layoutId, playlists) {

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val playlist = playlists[position]

            loadImage(holder.view.image_view_thumbnail, playlist.thumbnailUri)
            holder.view.text_view_title.text = playlist.title
            holder.view.text_view_description.text = playlist.description
            holder.view.setOnClickListener {
                startVideosFragment(playlist.id)
            }
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

        private fun startVideosFragment(playlistId: String) {
            requireFragmentManager().beginTransaction()
                .addToBackStack(null)
                .setCustomAnimations(
                    R.anim.anim_slide_in_left_without_fading,
                    R.anim.anim_slide_out_left_wihtout_fading,
                    R.anim.anim_slide_in_right_without_fading,
                    R.anim.anim_slide_out_right_without_fading
                ).replace(
                    R.id.constraint_layout_activity_youtube_channels,
                    YouTubeVideosFragment().apply {
                        setPlaylistId(playlistId)
                    }, TAG_YOUTUBE_VIDEOS_FRAGMENT
                ).commit()
        }

        fun addPlaylists(channelId: String) {
            (requireActivity() as YouTubeChannelsActivity).youTubeDataApi.getPlaylistsService()
                .requestByChannelId(channelId = channelId, pageToken = nextPageToken ?: "")
                .enqueue(object : Callback<PlaylistsModel> {
                    override fun onFailure(call: Call<PlaylistsModel>, t: Throwable) {
                        showToast(
                            requireContext(),
                            getString(R.string.failed_to_get_playlists)
                        )
                        t.printStackTrace()
                    }

                    override fun onResponse(
                        call: Call<PlaylistsModel>,
                        response: Response<PlaylistsModel>
                    ) {
                        val playlists = response.body()
                        if (playlists != null) {
                            val previousCount = items.count()
                            nextPageToken = playlists.nextPageToken
                            val playlistDataList =
                                playlists.items.map { createPlayListModel(it) } as ArrayList<PlaylistDataModel>

                            items.addAll(previousCount, playlistDataList)
                            CoroutineScope(Dispatchers.Main).launch {
                                recyclerView.itemAnimator?.changeDuration = 0
                                playlistsRecyclerViewAdapter.notifyItemRangeInserted(previousCount, playlistDataList.count())
                            }

                            (requireActivity() as YouTubeChannelsActivity).youtubeDataManager
                                .registerNextPageTokenAndPlaylists(channelId,
                                nextPageToken, items
                            )
                        } else {
                            (requireActivity() as YouTubeChannelsActivity).youtubeExceptionHandler
                                .youtubeDataApiExceptionHandling(
                                    YouTubeExceptionHandler.YouTubeResponseFailureException(
                                        "failed to get playlists",
                                        response.errorBody()!!
                                    ),
                                    getString(R.string.failed_to_get_playlists)
                                )
                        }
                    }
                })
        }

        private fun createPlayListModel(item: ItemModel)
                = PlaylistDataModel(
            id = item.id,
            title = item.snippet.title,
            description = item.snippet.description,
            thumbnailUri = item.snippet.thumbnails.standard?.url
                ?: item.snippet.thumbnails.default.url
        )
    }
}