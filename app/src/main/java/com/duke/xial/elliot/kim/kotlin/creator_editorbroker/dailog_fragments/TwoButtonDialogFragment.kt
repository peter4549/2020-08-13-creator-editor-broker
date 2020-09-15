package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.dailog_fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import kotlinx.android.synthetic.main.fragment_two_buttons_dialog.view.*

interface TwoButtonOnClickListener {
    fun setupFirstButton(text: String, onClickListener: View.OnClickListener?)
    fun setupSecondButton(text: String, onClickListener: View.OnClickListener?)
}

open class TwoButtonDialogFragment(private val title: String? = null,
                                   private val message: String? = null):
    DialogFragment(), TwoButtonOnClickListener {

    private lateinit var firstButton: Button
    private lateinit var secondButton: Button
    private var firstButtonText: String? = null
    private var secondButtonText: String? = null
    private var firstButtonOnClickListener: View.OnClickListener? = null
    private var secondButtonOnClickListener: View.OnClickListener? = null

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val view = requireActivity().layoutInflater.inflate(R.layout.fragment_two_buttons_dialog, null)

        view.text_view_title.text = title
        view.text_view_message.text = message

        if (firstButtonText != null) {
            firstButton = view.button_first
            firstButton.text = firstButtonText
            firstButton.setOnClickListener(firstButtonOnClickListener)
        }

        if (secondButtonText != null) {
            secondButton = view.button_second
            secondButton.text = secondButtonText
            secondButton.setOnClickListener(secondButtonOnClickListener)
        }

        builder.setView(view)
        return builder.create()
    }

    override fun setupFirstButton(text: String, onClickListener: View.OnClickListener?) {
        firstButtonText = text
        firstButtonOnClickListener = onClickListener
    }

    override fun setupSecondButton(text: String, onClickListener: View.OnClickListener?) {
        secondButtonText = text
        secondButtonOnClickListener = onClickListener
    }

}