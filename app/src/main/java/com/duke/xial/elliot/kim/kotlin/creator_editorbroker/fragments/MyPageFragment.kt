package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.activities.MainActivity
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.showToast
import kotlinx.android.synthetic.main.fragment_my_page.view.*

class MyPageFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_page, container, false)
        view.text_view_public_name.text = MainActivity.currentUserInformation?.publicName

        view.text_view_write_pr.setOnClickListener {
            (requireActivity() as MainActivity).startFragment(WritePrFragment(),
                R.id.constraint_layout_activity_main,
                MainActivity.TAG_WRITE_PR_FRAGMENT,
                MainActivity.VERTICAL)
        }
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance() = MyPageFragment()
    }
}