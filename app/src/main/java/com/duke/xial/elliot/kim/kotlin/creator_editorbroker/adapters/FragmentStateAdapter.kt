package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.fragments.MyPageFragment
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.fragments.PartnersTabFragment
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.pr.PrListFragment

class FragmentStateAdapter(fragmentActivity: FragmentActivity):
    androidx.viewpager2.adapter.FragmentStateAdapter(fragmentActivity) {
    private val pageCount = 3

    override fun getItemCount(): Int = pageCount

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> PrListFragment()//.newInstance()
            1 -> PartnersTabFragment.newInstance()
            2 -> MyPageFragment.newInstance()
            else -> throw Exception("invalid position")
        }
    }
}