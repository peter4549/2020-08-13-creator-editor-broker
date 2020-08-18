package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.youtube

import android.app.PendingIntent
import android.content.*
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.activities.MainActivity
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.error_handler.ErrorHandler
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.retrofit2.YouTubeDataApi
import kotlinx.android.synthetic.main.activity_youtube_channels.*

class YouTubeChannelsActivity: AppCompatActivity() {

    private var connection: CustomTabsServiceConnection? = null
    private lateinit var errorHandler: ErrorHandler
    private lateinit var youTubeDataApi: YouTubeDataApi

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
            //getAccessCode(code)
            unbindCustomTabsService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_youtube_channels)

        errorHandler = ErrorHandler(this)
        youTubeDataApi =
            YouTubeDataApi(
                this
            )

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

    /*

    inner class ChannelRecyclerViewAdapter(private val channels: ArrayList<ChannelModel>,
                                           layoutId: Int = R.layout.item_view_image)
        : BaseRecyclerViewAdapter<ChannelModel>(channels, layoutId)  {

        init {
            if (channelIds.isNotEmpty())
                insertChannelsByIds(channelIds)
        }

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

        private fun insertChannelById(channelId: String) {
            val request = youTubeDataApi.getChannelRequestById(channelId)
            val okHttpClient = OkHttpClient()
            okHttpClient.newCall(request).enqueue(object: Callback {
                override fun onFailure(call: Call, e: IOException) {
                    ErrorHandler.errorHandling(this@YouTubeChannelsActivity,
                        Exception("failed to register channel"),
                        getString(R.string.failed_to_register_channel))
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        try {
                            channels.add(0, youTubeDataApi.getChannelFromResponse(response))
                            CoroutineScope(Dispatchers.Main).launch {
                                notifyItemInserted(0)
                            }
                        } catch (e: Exception) {
                            ErrorHandler.errorHandling(this@YouTubeChannelsActivity,
                                e,
                                getString(R.string.failed_to_register_channel))
                        }
                    } else {
                        ErrorHandler.errorHandling(this@YouTubeChannelsActivity,
                            Exception("failed to register channel"),
                            getString(R.string.failed_to_register_channel))
                    }
                }
            })
        }

        inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_view_channel, parent, false)

            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return channels.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val channel = channels[position]

            holder.view.text_view_title.text = channel.title
            holder.view.text_view_description
            loadImage(holder.view.image_view, channel.thumbnailUri)

            holder.view.setOnClickListener {
                PlaylistsDialogFragment(
                    channel.id
                ).show(this@YouTubeChannelsActivity.supportFragmentManager,
                    TAG
                )
            }
        }

        private fun loadImage(imageView: ImageView, imageUri: String) {
            Glide.with(imageView.context)
                .load(imageUri)
                .error(R.drawable.ic_sentiment_dissatisfied_grey_24dp)
                .transition(DrawableTransitionOptions.withCrossFade())
                .transform(CenterCrop())
                .into(imageView)
        }

        fun insert(channelId: String) {
            insertChannelById(channelId)
        }
    }

     */

    companion object {
        const val ACTION_NEW_INTENT = "youtube.channels.activity.action.new.intent"
        const val KEY_AUTHORIZATION_CODE = "key_authorization_code"
        private const val CUSTOM_TAB_PACKAGE_NAME = "com.android.chrome"
        private const val TAG = "YouTubeChannelsActivity"
    }
}