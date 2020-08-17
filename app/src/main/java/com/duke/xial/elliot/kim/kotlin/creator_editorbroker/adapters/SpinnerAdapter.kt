package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import kotlinx.android.synthetic.main.item_view_spinner.view.*

class SpinnerAdapter<T: Any>(context: Context, private val items: Array<T>): BaseAdapter() {

    class ViewHolder(val textView: TextView)

    private val inflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val holder: ViewHolder

        if (convertView == null) {
            holder = ViewHolder(inflater.inflate(R.layout.item_view_spinner, parent, false)
                .text_view_spinner)
            holder.textView.tag = holder
        } else
            holder = convertView.tag as ViewHolder

        holder.textView.text = items[position].toString()

        return holder.textView
    }

    override fun getItem(position: Int): Any = items[position]

    override fun getItemId(position: Int): Long = 0L

    override fun getCount(): Int = items.count()
}