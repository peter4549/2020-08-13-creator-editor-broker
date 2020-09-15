package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.youtube

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.error_handler.ErrorHandler
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.error_handler.YouTubePlayerException
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.VideoDataModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.showToast
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.youtube.VideoClickDialogFragment.Companion.KEY_FROM_WRITE_PR_FRAGMENT
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.youtube.YouTubeChannelsActivity.Companion.KEY_VIDEO_DATA
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import kotlinx.android.synthetic.main.activity_youtube_player.*

class YouTubePlayerActivity : YouTubeBaseActivity() {

    private lateinit var video: VideoDataModel
    private lateinit var errorHandler: ErrorHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_youtube_player)

        errorHandler = ErrorHandler(this)
        video = intent.getSerializableExtra(KEY_VIDEO_DATA) as VideoDataModel

        text_view_title.text = video.title
        text_view_view_count.text = video.viewCount
        text_view_published_at.text = video.publishedAt
        text_view_description.text = video.description

        if (intent.getBooleanExtra(KEY_FROM_WRITE_PR_FRAGMENT, false))
            button_register.setOnClickListener {
                val intent = Intent()
                intent.putExtra(KEY_VIDEO_DATA, video)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        else
            button_register.visibility = View.GONE

        youtube_player_view.initialize(TAG, object: YouTubePlayer.OnInitializedListener {
            override fun onInitializationSuccess(
                provider: YouTubePlayer.Provider?,
                player: YouTubePlayer?,
                wasRestored: Boolean
            ) {
                if (!wasRestored)
                    player?.cueVideo(video.id)

                player?.setPlayerStateChangeListener(object: YouTubePlayer.PlayerStateChangeListener {
                    override fun onAdStarted() {  }

                    override fun onLoading() {  }

                    override fun onVideoStarted() {  }

                    override fun onLoaded(videoId: String?) {
                        player.play()
                    }

                    override fun onVideoEnded() {  }

                    override fun onError(reason: YouTubePlayer.ErrorReason?) {
                        errorHandler.errorHandling(YouTubePlayerException("failed to load video", reason!!))
                    }
                })
            }

            override fun onInitializationFailure(
                provider: YouTubePlayer.Provider?,
                result: YouTubeInitializationResult?
            ) {
                showToast(this@YouTubePlayerActivity, getString(R.string.failed_to_initialize_player))
            }
        })
    }

    companion object {
        private const val TAG = "YouTubePlayerActivity"
    }
}