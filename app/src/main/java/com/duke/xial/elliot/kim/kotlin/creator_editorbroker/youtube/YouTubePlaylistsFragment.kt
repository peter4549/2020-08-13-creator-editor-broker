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
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.ItemModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.PlaylistDataModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.PlaylistsModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.retrofit2.YouTubeDataApi
import kotlinx.android.synthetic.main.fragment_youtube_playlists.*
import kotlinx.android.synthetic.main.fragment_youtube_playlists.view.*
import kotlinx.android.synthetic.main.item_view_playlist.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class YouTubePlaylistsFragment: Fragment() {

    private var channelId: String? = null
    private var nextPageToken: String? = null

    fun setChannelId(channelId: String) {
        this.channelId = channelId
    }
// 액티비티에서 처리해서 던지는것도 고려..

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_youtube_playlists, container, false)
        /*
        if (channelId != null) {
            (requireActivity() as YouTubeChannelsActivity).youTubeDataApi.getPlaylistsService()
                .requestByChannelId(channelId = channelId!!).enqueue(object: Callback<PlaylistsModel> {
                    override fun onFailure(call: Call<PlaylistsModel>, t: Throwable) {
                        (requireActivity() as YouTubeChannelsActivity).errorHandler
                            .errorHandling(t, getString(R.string.failed_to_get_playlists))
                    }

                    override fun onResponse(
                        call: Call<PlaylistsModel>,
                        response: Response<PlaylistsModel>
                    ) {
                        val playlistsModel = response.body()
                        nextPageToken = playlistsModel?.nextPageToken
                        val playlistDataList = playlistsModel?.items?.map { createPlayListModel(it) }
                        CoroutineScope(Dispatchers.Main).launch {
                            recycler_view_playlists.apply {
                                adapter = PlaylistsRecyclerViewAdapter(
                                    playlistDataList as? ArrayList<PlaylistDataModel> ?:
                                    arrayListOf())
                                layoutManager = GridLayoutManagerWrapper(requireContext(), 1)
                            }
                        }
                    }
                })
        }

         */

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (channelId != null) {
            (requireActivity() as YouTubeChannelsActivity).youTubeDataApi.getPlaylistsService()
                .requestByChannelId(channelId = channelId!!).enqueue(object: Callback<PlaylistsModel> {
                    override fun onFailure(call: Call<PlaylistsModel>, t: Throwable) {
                        (requireActivity() as YouTubeChannelsActivity).errorHandler
                            .errorHandling(t, getString(R.string.failed_to_get_playlists))
                    }

                    override fun onResponse(
                        call: Call<PlaylistsModel>,
                        response: Response<PlaylistsModel>
                    ) {
                        val playlistsModel = response.body()
                        nextPageToken = playlistsModel?.nextPageToken
                        val playlistDataList = playlistsModel?.items?.map { createPlayListModel(it) }
                        CoroutineScope(Dispatchers.Main).launch {
                            recycler_view_playlists.apply {
                                adapter = PlaylistsRecyclerViewAdapter(
                                    playlistDataList as? ArrayList<PlaylistDataModel> ?:
                                    arrayListOf())
                                layoutManager = GridLayoutManagerWrapper(requireContext(), 1)
                            }
                        }
                    }
                })
        }
    }

    private fun createPlayListModel(item: ItemModel)
            = PlaylistDataModel(
        id = item.id,
        title = item.snippet.title,
        description = item.snippet.description,
        thumbnailUri = item.snippet.thumbnails.standard?.url ?: item.snippet.thumbnails.medium.url)

    inner class PlaylistsRecyclerViewAdapter(private val playlists: ArrayList<PlaylistDataModel>,
                                             layoutId: Int = R.layout.item_view_playlist)
        : BaseRecyclerViewAdapter<PlaylistDataModel>(playlists, layoutId) {

        init {
            //setPlaylists()
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val playlist = playlists[position]

            loadImage(holder.view.image_view_thumbnail, playlist.thumbnailUri)
            holder.view.text_view_title.text = playlist.title
            holder.view.text_view_description.text = playlist.description
        }

        private fun setPlaylists() {
            if (channelId != null) {
                (requireActivity() as YouTubeChannelsActivity).youTubeDataApi.getPlaylistsService()
                    .requestByChannelId(channelId = channelId!!).enqueue(object: Callback<PlaylistsModel> {
                        override fun onFailure(call: Call<PlaylistsModel>, t: Throwable) {
                            (requireActivity() as YouTubeChannelsActivity).errorHandler
                                .errorHandling(t, getString(R.string.failed_to_get_playlists))
                        }

                        override fun onResponse(
                            call: Call<PlaylistsModel>,
                            response: Response<PlaylistsModel>
                        ) {
                            val playlistsModel = response.body()
                            nextPageToken = playlistsModel?.nextPageToken
                            //playlists = playlistsModel?.items?.map { createPlayListModel(it) } as ArrayList<PlaylistDataModel>
                            CoroutineScope(Dispatchers.Main).launch {
                                notifyDataSetChanged()
                            }
                        }
                    })
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
    }
}