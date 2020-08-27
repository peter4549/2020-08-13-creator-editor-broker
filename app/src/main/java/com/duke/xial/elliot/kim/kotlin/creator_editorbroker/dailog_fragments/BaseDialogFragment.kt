package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.dailog_fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R

interface DynamicChildViewDialog {
    fun setTitle(text: String)
    fun addTextView(tag: String, text: String, drawableResourceId: Int?, onClickListener: View.OnClickListener?)
}

open class BaseDialogFragment: DialogFragment(), DynamicChildViewDialog {

    private lateinit var linearLayout: LinearLayout
    private var viewCount = 0

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        linearLayout = requireActivity().layoutInflater.inflate(R.layout.fragment_base_dialog, null) as LinearLayout
        builder.setView(linearLayout)
        return builder.create()
    }

    override fun setTitle(text: String) {
        val textViewTitle = TextView(requireContext()).apply{
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
            isClickable = false
            isFocusable = false
            gravity = Gravity.CENTER
            setPadding(8, 16, 8, 16)
            setText(text)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 24F)
        }

        linearLayout.addView(textViewTitle, 0)
        ++viewCount
    }

    override fun addTextView(
        tag: String,
        text: String,
        drawableResourceId: Int?,
        onClickListener: View.OnClickListener?
    ) {
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true)
        val textView = TextView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
            isClickable = true
            isFocusable = true
            setBackgroundResource(typedValue.resourceId)
            gravity = Gravity.CENTER
            setPadding(8, 16, 8, 16)
            setTag(tag)
            setText(text)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)
            setOnClickListener(onClickListener)
            if (drawableResourceId != null)
                setCompoundDrawablesWithIntrinsicBounds(drawableResourceId, 0, 0, 0)
        }

        linearLayout.addView(textView)
    }
}