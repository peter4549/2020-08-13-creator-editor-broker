package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.adapters.PartnersFragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_partners.view.*

class PartnersFragment : Fragment() {

    private lateinit var tabIconResourceIds: Array<Int>
    private lateinit var tabTexts: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tabIconResourceIds = arrayOf(
            R.drawable.ic_round_people_24,
            R.drawable.ic_speech_bubble_24
        )

        tabTexts = arrayOf(
            getString(R.string.my_partners),
            getString(R.string.chat)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_partners, container, false)
        initViewPagerAndTabLayout(view.view_pager, view.tab_layout)
        return view
    }

    private fun initViewPagerAndTabLayout(viewPager: ViewPager2, tabLayout: TabLayout) {
        viewPager.adapter = PartnersFragmentStateAdapter(requireActivity())

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.tag = position
            tab.text = tabTexts[position]
            tab.setIcon(tabIconResourceIds[position])
        }.attach()
    }

    companion object {
        @JvmStatic
        fun newInstance() = PartnersFragment()
    }
}