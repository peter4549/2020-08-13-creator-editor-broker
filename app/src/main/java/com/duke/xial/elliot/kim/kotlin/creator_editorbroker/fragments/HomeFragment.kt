package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.adapters.HomeFragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {

    private lateinit var tabIconResourceIds: Array<Int>
    private lateinit var tabTexts: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tabIconResourceIds = arrayOf(
            R.drawable.ic_round_list_alt_24,
            R.drawable.ic_round_people_24
        )

        tabTexts = arrayOf(
            getString(R.string.pr_list),
            getString(R.string.partners)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewPagerAndTabLayout()
    }

    private fun initViewPagerAndTabLayout() {
        view_pager.adapter = HomeFragmentStateAdapter(requireActivity())

        TabLayoutMediator(tab_layout, view_pager) { tab, position ->
            tab.tag = position
            tab.text = tabTexts[position]
            tab.setIcon(tabIconResourceIds[position])
        }.attach()
    }

    companion object {
        @JvmStatic
        fun newInstance() = HomeFragment()
    }
}