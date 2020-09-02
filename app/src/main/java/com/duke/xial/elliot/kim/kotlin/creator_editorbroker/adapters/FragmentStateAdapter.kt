package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.fragments.HomeFragment
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.fragments.MyPageFragment
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.fragments.PartnersTabFragment

class FragmentStateAdapter(fragmentActivity: FragmentActivity):
    androidx.viewpager2.adapter.FragmentStateAdapter(fragmentActivity) {
    private val pageCount = 3

    override fun getItemCount(): Int = pageCount

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment.newInstance()
            1 -> PartnersTabFragment.newInstance()
            2 -> MyPageFragment.newInstance()
            else -> throw Exception("invalid position")
        }
    }
}