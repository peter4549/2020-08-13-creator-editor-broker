package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.youtube

import android.app.PendingIntent
import android.content.*
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.ChannelModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.ChannelsItemsModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.retrofit2.YouTubeDataApi
import com.google.gson.internal.LinkedTreeMap
import kotlinx.android.synthetic.main.activity_youtube_channels.*
import kotlinx.android.synthetic.main.item_view_channel.view.*
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

    override fun onResume() {
        super.onResume()
        unbindCustomTabsService()
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
                    val accessToken =(response.body() as LinkedTreeMap<String, Any>)["access_token"] as String
                    getChannels(accessToken)
                }
            })

        /*
        youTubeDataApi..getPlaylistItemsService()
            .requestPlaylistItems().enqueue(object:
                Callback<PlaylistItemsModel> {
                override fun onFailure(call: Call<PlaylistItemsModel>, t: Throwable) {
                    errorHandler.errorHandling(t, "플레이리스트를 불러오지 못했습니다.")
                }

                override fun onResponse(
                    call: Call<PlaylistItemsModel>,
                    response: Response<PlaylistItemsModel>
                ) {
                    if (response.isSuccessful) {
                        val videoIds =
                            response.body()?.items?.map { it.contentDetails }
                                ?.joinToString(",") { it.videoId }
                        if (videoIds != null) {
                            getVideos(tabLayout, viewPager, videoIds)
                        } else
                            errorHandler.errorHandling(
                                Exception("failed to load playlist, videoIds is null, unknown exception"),
                                getString(R.string.failed_to_load_playlist), throwing = true)
                    } else
                        response.errorBody()?.let {
                            ResponseFailedException(
                                "failed to load playlist",
                                it
                            )
                        }?.let { errorHandler.errorHandling(it, getString(R.string.failed_to_load_playlist)) }
                }
            })

         */

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
                    response.body()?.let {
                        channelRecyclerViewAdapter.insert(createChannelModel(it))
                    } ?: run {
                        errorHandler.errorHandling(NullPointerException("failed to get channels"),
                            getString(R.string.failed_to_get_channels))
                    }
                }
            })
    }

    private fun createChannelModel(channelsItemsModel: ChannelsItemsModel): ChannelModel {
        val channelItem = channelsItemsModel.items[0]
        return ChannelModel(id = channelItem.id,
            title = channelItem.snippet.title,
            thumbnailUri = channelItem.snippet.thumbnails.medium.url)
    }

    /*
    private fun getAccessCode(code: String) {
        val request = youTubeDataApi.getAuthorizationRequest(code)

        val okHttpClient = OkHttpClient()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                ErrorHandler.errorHandling(this@YouTubeChannelsActivity, e,
                    getString(R.string.failed_to_get_access_token))
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val map: Map<*, *>? =
                        Gson().fromJson(response.body?.string(), Map::class.java)
                    getChannelId(map?.get("access_token") as String) // 채널 아이디를 가져옴. 이 액세스 코드를 이용. 얘를 리턴하는 식으로 flatten할수잇으면 좋은디.
                } else
                    ErrorHandler.errorHandling(this@YouTubeChannelsActivity,
                        ResponseFailedException("response failed, failed to get access token", response),
                        getString(R.string.failed_to_get_access_token))
            }
        })
    }

     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_youtube_channels)
        // 여기서 아이디로부터 채널 생성 해야함.

        channelRecyclerViewAdapter = ChannelRecyclerViewAdapter(arrayListOf()) // 여기에 메인 유저 모델로부터 얻은 채널리스트 가져올것.

        errorHandler = ErrorHandler(this)
        youTubeDataApi = YouTubeDataApi(this)

        recycler_view_channels.apply {
            adapter = channelRecyclerViewAdapter
            layoutManager = GridLayoutManagerWrapper(this@YouTubeChannelsActivity, 1)
        }

        if (MainActivity.currentUserInformation?.channelIds?.isEmpty()!!)
            text_view_empty.visibility = View.VISIBLE
        else
            text_view_empty.visibility = View.GONE

        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.addPrimaryClipChangedListener {

        }

        frame_layout_add_channel.setOnClickListener {
            bindCustomTabsService()
        }

        //channelsRecyclerViewAdapter = ChannelsRecyclerViewAdapter(user.channelIds)

        /*
        recycler_view.apply {
            adapter = channelsRecyclerViewAdapter
            layoutManager = GridLayoutManagerWrapper(this@YouTubeChannelsActivity, 1)
        }

         */
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
                startPlaylistsFragment(channel.id)
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

        private fun startPlaylistsFragment(channelId: String) {
            supportFragmentManager.beginTransaction()
                .addToBackStack(null)
                .setCustomAnimations(
                    R.anim.anim_slide_in_left,
                    R.anim.anim_slide_out_left,
                    R.anim.anim_slide_in_right,
                    R.anim.anim_slide_out_right
                ).replace(R.id.constraint_layout_activity_youtube_channels,
                    YouTubePlaylistsFragment().apply {
                        setChannelId(channelId)
                    }, TAG_YOUTUBE_PLAYLISTS_FRAGMENT)
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

/* 이거슨 채널을 아이도로 찾아 채널 객체를 찾아오는 거시다.
private fun insertChannelsByIds(channelIds: MutableList<String>) {
            val request = youTubeDataApi.getChannelsRequestByIds(channelIds)
            val okHttpClient = OkHttpClient()
            var channelCount = 0
            okHttpClient.newCall(request).enqueue(object: Callback {
                override fun onFailure(call: Call, e: IOException) {
                    ErrorHandler.errorHandling(this@YouTubeChannelsActivity, e,
                        getString(R.string.failed_to_load_channels))
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {

                        try {
                            channels = youTubeDataApi.getChannelsFromResponse(response)
                            CoroutineScope(Dispatchers.Main).launch {
                                notifyItemInserted(0)
                            }

                            channelCount = channels.count()

                            if (user.channelIds.count() > channelCount)
                                throw TypeCastException()
                        } catch (e: TypeCastException) {
                            val map = Gson().fromJson(response.body?.string(), Map::class.java)
                            val pageInfo = map["pageInfo"] as LinkedTreeMap<*, *>
                            val totalResults = pageInfo["totalResults"] as Double

                            if (totalResults == 0.0)
                                ErrorHandler.errorHandling(this@YouTubeChannelsActivity,
                                    e, getString(R.string.channels_not_found))
                            else{
                                val notFoundChannelsCount = user.channelIds.count() - channelCount
                                ErrorHandler.errorHandling(this@YouTubeChannelsActivity,
                                    e, notFoundChannelsCount.toString() + getString(R.string.n_channels_not_found))
                            }
                        } catch (e: Exception) {
                            ErrorHandler.errorHandling(this@YouTubeChannelsActivity, e,
                                getString(R.string.failed_to_load_channels))
                        }
                    } else {
                        ErrorHandler.errorHandling(
                            this@YouTubeChannelsActivity,
                            ResponseFailedException("failed to load channels", response),
                            getString(R.string.failed_to_load_channels)
                        )
                    }
                }
            })
        }
 */