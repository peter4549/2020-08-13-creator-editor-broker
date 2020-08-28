package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.activities.MainActivity
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.PrModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.UserInformationModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.VideoDataModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.scaleDown
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.scaleUp
import kotlinx.android.synthetic.main.fragment_pr.*
import kotlinx.android.synthetic.main.fragment_pr.view.*

class PrFragment(private val pr: PrModel? = null): Fragment() {

    private lateinit var fragmentView: View
    private lateinit var youtubeVideos: Array<VideoDataModel>
    private var isFabOpen = false
    private val fabClickListener = View.OnClickListener { view ->
        when(view.id) {
            R.id.fab_eject -> {
                fab_eject.isMovable = isFabOpen
                animateFab()
            }
            R.id.fab_chat -> {
                startChatFragment(pr?.userInformation!!)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return if (::fragmentView.isInitialized)
            fragmentView
        else {
            fragmentView = inflater.inflate(R.layout.fragment_pr, container, false)
            youtubeVideos = pr?.youtubeVideos?.filterNotNull()?.toTypedArray() ?: arrayOf()
            initializeViewPager(fragmentView.view_pager)
            initializeFab(fragmentView)

            fragmentView.text_view_title.text = pr?.title
            fragmentView.text_view_public_name.text = pr?.publisherPublicName

            fragmentView
        }
    }

    private fun initializeViewPager(viewPager: ViewPager) {
        viewPager.adapter = ImageViewPagerAdapter(requireFragmentManager())
    }

    private fun initializeFab(view: View) {
        view.fab_chat.setOnClickListener(fabClickListener)
        view.fab_eject.setOnClickListener(fabClickListener)
        view.fab_favorite.setOnClickListener(fabClickListener)
        view.fab_star.setOnClickListener(fabClickListener)
        view.fab_chat.isMovable = false
        view.fab_favorite.isMovable = false
        view.fab_star.isMovable = false
        view.fab_chat.scaleDown()
        view.fab_favorite.scaleDown()
        view.fab_star.scaleDown()
        view.fab_eject.setChildButtons(view.fab_chat, view.fab_favorite, view.fab_star)
    }

    private fun disableFab() {
        fab_chat.visibility = View.GONE
        fab_eject.visibility = View.GONE
        fab_favorite.visibility = View.GONE
        fab_star.visibility = View.GONE
    }

    private fun animateFab() {
        if (isFabOpen) {
            fab_eject.animate().rotation(0F).setDuration(240L).start()
            fab_chat.scaleDown()
            fab_favorite.scaleDown()
            fab_star.scaleDown()
            fab_chat.isEnabled = false
            fab_favorite.isEnabled = false
            fab_star.isEnabled = false
        } else {
            fab_eject.animate().rotation(45F).setDuration(240L).start()
            fab_chat.scaleUp()
            fab_favorite.scaleUp()
            fab_star.scaleUp()
            fab_chat.isEnabled = true
            fab_favorite.isEnabled = true
            fab_star.isEnabled = true
        }

        isFabOpen = !isFabOpen
    }

    private fun startChatFragment(targetUser: UserInformationModel) {
        (requireActivity() as MainActivity).startFragment(
            ChatFragment(targetUser),
            R.id.frame_layout_activity_main, MainActivity.TAG_CHAT_FRAGMENT
        )
    }

    inner class ImageViewPagerAdapter(fragmentManager: FragmentManager) :
        FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getCount(): Int = youtubeVideos.count()

        override fun getItem(position: Int): Fragment = SingleImageViewFragment(youtubeVideos[position].thumbnailUri)
    }
}