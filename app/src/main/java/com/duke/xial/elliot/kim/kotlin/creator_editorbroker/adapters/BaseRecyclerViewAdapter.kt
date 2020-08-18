package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("unused")
open class BaseRecyclerViewAdapter<T: Any?>(private val items: ArrayList<T> = arrayListOf(),
                                            private val layoutId: Int):
    RecyclerView.Adapter<BaseRecyclerViewAdapter<T>.ViewHolder>() {

    private lateinit var recyclerView: RecyclerView

    @Suppress("MemberVisibilityCanBePrivate")
    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.count()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

    }

    fun insert(item: T, position: Int = 0) {
        items.add(position, item)
        notifyItemInserted(position)
    }

    fun update(item: T) {
        notifyItemChanged(items.indexOf(item))
    }

    fun update(position: Int) {
        notifyItemChanged(position)
    }

    fun remove(item: T, millisecondNotificationDelay: Long = 0L) {
        val position = items.indexOf(item)
        items.remove(item)
        CoroutineScope(Dispatchers.IO).launch {
            delay(millisecondNotificationDelay)
            launch(Dispatchers.Main) {
                notifyItemRemoved(position)
            }
        }
    }

    fun remove(position: Int, millisecondNotificationDelay: Long = 0L) {
        items.removeAt(position)
        CoroutineScope(Dispatchers.IO).launch {
            delay(millisecondNotificationDelay)
            launch(Dispatchers.Main) {
                notifyItemRemoved(position)
            }
        }
    }
}