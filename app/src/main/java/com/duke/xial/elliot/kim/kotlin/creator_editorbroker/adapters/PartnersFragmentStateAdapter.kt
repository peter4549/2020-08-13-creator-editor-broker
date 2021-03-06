package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.activities.MainActivity

class PartnersFragmentStateAdapter(private val fragmentActivity: FragmentActivity):
    FragmentStateAdapter(fragmentActivity) {
    private val pageCount = 2

    override fun getItemCount(): Int = pageCount

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> (fragmentActivity as MainActivity).myPartnersFragment
            1 -> (fragmentActivity as MainActivity).chatRoomsFragment
            else -> throw Exception("invalid fragment")
        }
    }
}