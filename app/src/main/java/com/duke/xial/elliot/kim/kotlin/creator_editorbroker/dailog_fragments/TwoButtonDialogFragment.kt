package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.dailog_fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import kotlinx.android.synthetic.main.fragment_two_buttons_dialog.view.*

interface TwoButtonOnClickListener {
    fun setFirstOnClickListener(text: String, onClickListener: View.OnClickListener?)
    fun setSecondOnClickListener(text: String, onClickListener: View.OnClickListener?)
}

open class TwoButtonDialogFragment(private val title: String? = null,
                                   private val message: String? = null):
    DialogFragment(), TwoButtonOnClickListener {

    private lateinit var buttonFirst: Button
    private lateinit var buttonSecond: Button

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val view = requireActivity().layoutInflater.inflate(R.layout.fragment_two_buttons_dialog, null)

        view.text_view_title.text = title
        view.text_view_message.text = message
        buttonFirst = view.button_first
        buttonSecond = view.button_second

        builder.setView(view)
        return builder.create()
    }

    override fun setFirstOnClickListener(text: String, onClickListener: View.OnClickListener?) {
        buttonFirst.text = text
        buttonFirst.setOnClickListener(onClickListener)
    }

    override fun setSecondOnClickListener(text: String, onClickListener: View.OnClickListener?) {
        buttonSecond.text = text
        buttonSecond.setOnClickListener(onClickListener)
    }

}