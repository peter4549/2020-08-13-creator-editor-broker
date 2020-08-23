package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.youtube

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.constants.REQUEST_CODE_YOUTUBE_PLAYER
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.fragments.BaseDialogFragment
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.VideoDataModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.showToast
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.youtube.YouTubeChannelsActivity.Companion.KEY_VIDEO_DATA

class VideoClickDialogFragment(private val video: VideoDataModel? = null): BaseDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        setTitle(video?.title ?: "")
        addTextView("button_play", getString(R.string.play_video), R.drawable.ic_baseline_play_circle_filled_24, onClickListener)
        addTextView("button_register", getString(R.string.add_to_my_works), R.drawable.ic_round_check_circle_24, onClickListener)
        return dialog
    }

    override fun onResume() {
        super.onResume()
        val width = resources.getDimensionPixelSize(R.dimen.video_click_dialog_width)
        dialog?.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
    }

    private val onClickListener = View.OnClickListener { view: View ->
        when(view.tag) {
            "button_play" -> {
                if (video != null)
                    startYouTubePlayerActivity(video)
                else
                    showToast(requireContext(), getString(R.string.please_try_again))
            }
            "button_register" -> {
                if (video != null)
                    (requireActivity() as YouTubeChannelsActivity).registerVideo(video)
                else
                    showToast(requireContext(), getString(R.string.failed_to_register_video))
            }
        }
    }

    private fun startYouTubePlayerActivity(video: VideoDataModel) {
        val intent = Intent(requireActivity(), YouTubePlayerActivity::class.java)
        intent.putExtra(KEY_FROM_WRITE_PR_FRAGMENT, true)
        intent.putExtra(KEY_VIDEO_DATA, video)
        requireActivity().startActivityForResult(intent, REQUEST_CODE_YOUTUBE_PLAYER)
    }

    companion object {
        const val KEY_FROM_WRITE_PR_FRAGMENT = "key_from_write_pr_fragment"
    }
}