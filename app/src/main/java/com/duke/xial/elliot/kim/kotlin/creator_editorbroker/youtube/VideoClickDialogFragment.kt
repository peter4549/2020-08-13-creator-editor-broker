package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.youtube

import android.app.Dialog
import android.os.Bundle
import android.view.WindowManager
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.fragments.BaseDialogFragment


class VideoClickDialogFragment: BaseDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        setTitle("유튜브 테스트.")
        addTextView("비디오 재생", R.drawable.ic_baseline_play_circle_filled_24, null)
        addTextView("내 작품에 추가", R.drawable.ic_round_check_circle_24, null)
        return dialog
    }

    override fun onResume() {
        super.onResume()
        val width = resources.getDimensionPixelSize(R.dimen.video_click_dialog_width)
        dialog?.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
    }
}