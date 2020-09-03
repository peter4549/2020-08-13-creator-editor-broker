package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.dailog_fragments

import android.app.Dialog
import android.os.Bundle
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.activities.MainActivity

class RequestProfileCreationDialogFragment(title: String? = null,
                                           message: String? = null): TwoButtonDialogFragment(title, message) {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        setFirstOnClickListener(getString(R.string.cancel)) {
            dismiss()
        }

        setSecondOnClickListener(getString(R.string.ok)) {
            (requireActivity() as MainActivity).startCreateProfileFragment()
            dismiss()
        }

        return dialog
    }
}