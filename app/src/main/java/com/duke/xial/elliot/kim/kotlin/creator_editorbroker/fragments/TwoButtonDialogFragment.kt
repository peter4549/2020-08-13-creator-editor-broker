package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import kotlinx.android.synthetic.main.fragment_two_button_dialog.view.*

interface TwoButtonOnClickListener {
    fun setFirstOnClickListener(onClickListener: View.OnClickListener?)
    fun setSecondOnClickListener(onClickListener: View.OnClickListener?)
}

open class TwoButtonDialogFragment: DialogFragment(), TwoButtonOnClickListener {

    protected lateinit var buttonFirst: Button
    protected lateinit var buttonSecond: Button

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val view = requireActivity().layoutInflater.inflate(R.layout.fragment_two_button_dialog, null)

        buttonFirst = view.button_first
        buttonSecond = view.button_second

        builder.setView(view)
        return builder.create()
    }

    override fun setFirstOnClickListener(onClickListener: View.OnClickListener?) {
        buttonFirst.setOnClickListener(onClickListener)
    }

    override fun setSecondOnClickListener(onClickListener: View.OnClickListener?) {
        buttonSecond.setOnClickListener(onClickListener)
    }

}