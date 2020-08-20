package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.youtube

import android.app.PendingIntent
import android.content.*
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.activities.MainActivity
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.adapters.BaseRecyclerViewAdapter
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.adapters.GridLayoutManagerWrapper
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.error_handler.ErrorHandler
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.error_handler.ResponseFailureException
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.*
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.retrofit2.YouTubeDataApi
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.showToast
import com.google.gson.internal.LinkedTreeMap
import kotlinx.android.synthetic.main.activity_youtube_channels.*
import kotlinx.android.synthetic.main.item_view_channel.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class YouTubeChannelsActivity: AppCompatActivity() {

    private var connection: CustomTabsServiceConnection? = null
    private lateinit var channelRecyclerViewAdapter: ChannelRecyclerViewAdapter
    lateinit var errorHandler: ErrorHandler
    lateinit var youTubeDataApi: YouTubeDataApi

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == CheckUrlService.ACTION_CORRECT_URL) {
                val code = intent.getStringExtra(CheckUrlService.KEY_AUTHORIZATION_CODE) ?: CheckUrlService.INVALID_CODE
                if (code == CheckUrlService.INVALID_CODE)
                    errorHandler.errorHandling(Exception("failed to get authorization code"),
                        getString(R.string.failed_to_authorization))
                val selfIntent = Intent(this@YouTubeChannelsActivity, YouTubeChannelsActivity::class.java)

                selfIntent.action = ACTION_NEW_INTENT
                selfIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                selfIntent.putExtra(KEY_AUTHORIZATION_CODE, code)
                startActivity(selfIntent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_youtube_channels)

        errorHandler = ErrorHandler(this)
        youTubeDataApi = YouTubeDataApi(this)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (MainActivity.currentUserInformation?.channelIds?.isEmpty()!!)
            text_view_empty.visibility = View.VISIBLE
        else
            text_view_empty.visibility = View.GONE

        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.addPrimaryClipChangedListener {
            // Guiding message.
        }

        frame_layout_add_channel.setOnClickListener {
            bindCustomTabsService()
        }

        if (MainActivity.currentUserInformation!!.channelIds.isNotEmpty()) {
            val joinedChannelIds =
                MainActivity.currentUserInformation!!.channelIds.joinToString(",")
            youTubeDataApi.getChannelsService().requestById(id = joinedChannelIds)
                .enqueue(object : Callback<ChannelsItemsModel> {
                    override fun onFailure(call: Call<ChannelsItemsModel>, t: Throwable) {
                        errorHandler.errorHandling(t, getString(R.string.failed_to_get_channels))
                        channelRecyclerViewAdapter = ChannelRecyclerViewAdapter(arrayListOf())
                        CoroutineScope(Dispatchers.Main).launch {
                            recycler_view_channels.apply {
                                adapter = channelRecyclerViewAdapter
                                layoutManager =
                                    GridLayoutManagerWrapper(this@YouTubeChannelsActivity, 1)
                            }
                        }
                    }

                    override fun onResponse(
                        call: Call<ChannelsItemsModel>,
                        response: Response<ChannelsItemsModel>
                    ) {
                        val responseBody = response.body()
                        if (responseBody != null) {
                            val channelItems = responseBody.items
                            val difference = MainActivity.currentUserInformation!!.channelIds.count() -
                                    channelItems.count()
                            if (difference > 0)
                                showToast(this@YouTubeChannelsActivity,
                                    "$difference ${getString(R.string.n_channel_was_not_found)}")

                            val channels = response.body()?.items?.map {
                                createChannelModel(it)
                            } as ArrayList<ChannelModel>
                            channelRecyclerViewAdapter = ChannelRecyclerViewAdapter(channels)
                            CoroutineScope(Dispatchers.Main).launch {
                                recycler_view_channels.apply {
                                    adapter = channelRecyclerViewAdapter
                                    layoutManager =
                                        GridLayoutManagerWrapper(this@YouTubeChannelsActivity, 1)
                                }
                            }
                        } else {
                            errorHandler
                                .errorHandling(ResponseFailureException("failed to get channels",
                                    response.errorBody()!!))
                        }
                    }
                })
        } else {
            channelRecyclerViewAdapter = ChannelRecyclerViewAdapter(arrayListOf())
            recycler_view_channels.apply {
                adapter = channelRecyclerViewAdapter
                layoutManager =
                    GridLayoutManagerWrapper(this@YouTubeChannelsActivity, 1)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        unbindCustomTabsService()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        when {
            supportFragmentManager.findFragmentByTag(TAG_YOUTUBE_PLAYLISTS_FRAGMENT) != null -> super.onBackPressed()
            else -> super.onBackPressed()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.action == ACTION_NEW_INTENT) {
            val code = intent.getStringExtra(KEY_AUTHORIZATION_CODE) as String
            getAccessTokenChannels(code)
            unbindCustomTabsService()
        }
    }

    private fun getAccessTokenChannels(code: String) {
        youTubeDataApi.getAuthorizationService()
            .requestAuthorization(code).enqueue(object: Callback<LinkedTreeMap<String, Any>> {
                override fun onFailure(call: Call<LinkedTreeMap<String, Any>>, t: Throwable) {
                    errorHandler.errorHandling(t, getString(R.string.failed_to_get_access_token))
                }

                override fun onResponse(
                    call: Call<LinkedTreeMap<String, Any>>,
                    response: Response<LinkedTreeMap<String, Any>>
                ) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val accessToken =
                            responseBody["access_token"] as String
                        getChannels(accessToken)
                    } else {
                        showToast(this@YouTubeChannelsActivity,
                            getString(R.string.invalid_authorization_code))
                    }
                }
            })
    }

    private fun getChannels(accessToken: String) {
        youTubeDataApi.getChannelsService().requestByAccessToken("Bearer $accessToken")
            .enqueue(object: Callback<ChannelsItemsModel>{
                override fun onFailure(call: Call<ChannelsItemsModel>, t: Throwable) {
                    errorHandler.errorHandling(t, getString(R.string.failed_to_get_channels))
                }

                override fun onResponse(
                    call: Call<ChannelsItemsModel>,
                    response: Response<ChannelsItemsModel>
                ) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val channelItem = responseBody.items[0]
                        if (MainActivity.currentUserInformation!!.channelIds.contains(channelItem.id))
                            showToast(this@YouTubeChannelsActivity,
                                getString(R.string.channel_is_already_registered))
                        else {
                            MainActivity.currentUserInformation!!.channelIds.add(channelItem.id)
                            channelRecyclerViewAdapter.insert(createChannelModel(channelItem))
                            MainActivity.channelIdsChanged = true
                        }
                    } else {
                        errorHandler
                            .errorHandling(ResponseFailureException("failed to get channels",
                                response.errorBody()!!))
                    }
                }
            })
    }

    private fun createChannelModel(channelItem: ItemModel): ChannelModel {
        return ChannelModel(id = channelItem.id,
            title = channelItem.snippet.title,
            thumbnailUri = channelItem.snippet.thumbnails.medium.url)
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,
            IntentFilter(CheckUrlService.ACTION_CORRECT_URL)
        )
    }

    private fun bindCustomTabsService() {
        if (connection != null)
            return

        connection = object : CustomTabsServiceConnection() {
            override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient
            ) {
                val builder = CustomTabsIntent.Builder()
                val icon = BitmapFactory.decodeResource(resources, android.R.drawable.ic_btn_speak_now)
                val title = getString(R.string.complete)

                builder.setToolbarColor(ContextCompat.getColor(applicationContext, R.color.colorFragmentBackground))
                builder.setActionButton(icon, title, createPendingIntent(), true)
                builder.setExitAnimations(applicationContext, R.anim.anim_slide_in_left, R.anim.anim_slide_out_left)
                builder.setStartAnimations(applicationContext, R.anim.anim_slide_in_right, R.anim.anim_slide_out_right)

                val customTabsIntent = builder.build()
                customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                customTabsIntent.intent.setPackage(CUSTOM_TAB_PACKAGE_NAME)
                customTabsIntent.launchUrl(this@YouTubeChannelsActivity, Uri.parse(
                    youTubeDataApi.getGoogleAuthorizationServerUrl()
                ))
            }

            override fun onServiceDisconnected(componentName: ComponentName?) {
                println("$TAG: custom tabs service disconnected")
            }
        }

        if (CustomTabsClient.bindCustomTabsService(this,
                CUSTOM_TAB_PACKAGE_NAME, connection!!))
            println("$TAG: custom tabs service connected")
        else
            errorHandler.errorHandling(Exception("failed to connect custom tabs service"),
                getString(R.string.failed_to_connect_authorization_service))
    }

    private fun createPendingIntent() : PendingIntent {
        val intent = Intent(this, CheckUrlService::class.java)
        return PendingIntent.getService(this, 0, intent, 0)
    }

    private fun unbindCustomTabsService() {
        if (connection == null)
            return
        else {
            this.unbindService(connection!!)
            connection = null
        }
    }

    inner class ChannelRecyclerViewAdapter(private val channels: ArrayList<ChannelModel>,
                                           layoutId: Int = R.layout.item_view_channel)
        : BaseRecyclerViewAdapter<ChannelModel>(channels, layoutId)  {

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val channel = channels[position]

            loadImage(holder.view.image_view_thumbnail, channel.thumbnailUri)
            holder.view.text_view_title.text = channel.title
            holder.view.setOnClickListener {
                setPlaylistsByChannelIdAndStartPlaylistsFragment(channel.id)
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

        private fun setPlaylistsByChannelIdAndStartPlaylistsFragment(channelId: String) {
                youTubeDataApi.getPlaylistsService()
                    .requestByChannelId(channelId = channelId)
                    .enqueue(object : Callback<PlaylistsModel> {
                        override fun onFailure(call: Call<PlaylistsModel>, t: Throwable) {
                            errorHandler.errorHandling(t, getString(R.string.failed_to_get_playlists))
                        }

                        override fun onResponse(
                            call: Call<PlaylistsModel>,
                            response: Response<PlaylistsModel>
                        ) {
                            val playlistsModel = response.body()
                            val nextPageToken = playlistsModel?.nextPageToken
                            val playlistDataList =
                                playlistsModel?.items?.map { createPlayListModel(it) } as ArrayList<PlaylistDataModel>
                            val youtubePlaylistsFragment =
                                YouTubePlaylistsFragment().apply {
                                    setNextToken(nextPageToken)
                                    setPlaylists(playlistDataList)
                                }

                            CoroutineScope(Dispatchers.Main).launch {
                                startPlaylistsFragment(youtubePlaylistsFragment)
                            }
                        }
                    })
        }

        private fun createPlayListModel(item: ItemModel)
                = PlaylistDataModel(
            id = item.id,
            title = item.snippet.title,
            description = item.snippet.description,
            thumbnailUri = item.snippet.thumbnails.standard?.url ?: item.snippet.thumbnails.medium.url)

        private fun startPlaylistsFragment(playlistsFragment: YouTubePlaylistsFragment) {
            supportFragmentManager.beginTransaction()
                .addToBackStack(null)
                .setCustomAnimations(
                    R.anim.anim_slide_in_left,
                    R.anim.anim_slide_out_left,
                    R.anim.anim_slide_in_right,
                    R.anim.anim_slide_out_right
                ).replace(R.id.constraint_layout_activity_youtube_channels,
                    playlistsFragment, TAG_YOUTUBE_PLAYLISTS_FRAGMENT)
                .commit()
        }
    }

    companion object {
        const val ACTION_NEW_INTENT = "youtube.channels.activity.action.new.intent"
        const val KEY_AUTHORIZATION_CODE = "key_authorization_code"
        private const val CUSTOM_TAB_PACKAGE_NAME = "com.android.chrome"
        private const val TAG = "YouTubeChannelsActivity"
        private const val TAG_YOUTUBE_PLAYLISTS_FRAGMENT = "tag_youtube_playlists_fragment"
    }
}