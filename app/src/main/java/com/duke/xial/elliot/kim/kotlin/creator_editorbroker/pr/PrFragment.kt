package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.pr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.activities.MainActivity
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.activities.MainActivity.Companion.currentUser
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.activities.MainActivity.Companion.errorHandler
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.constants.PR_LIST
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.fragments.SingleImageViewFragment
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.PrModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.PrModel.Companion.KEY_FAVORITE_USER_IDS
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.UserModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.VideoDataModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.profiles.ProfileFragment
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.scaleDown
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.scaleUp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.android.synthetic.main.fragment_pr.*
import kotlinx.android.synthetic.main.fragment_pr.view.*
import java.lang.Exception
import java.lang.NullPointerException

class PrFragment(private val pr: PrModel? = null): Fragment() {

    private lateinit var fragmentView: View
    private lateinit var prDocumentReference: DocumentReference
    private lateinit var prListenerRegistration: ListenerRegistration
    private lateinit var youtubeVideos: Array<VideoDataModel>
    private var isFabOpen = false
    private val fabClickListener = View.OnClickListener { view ->
        when(view.id) {
            R.id.fab_eject -> {
                fab_eject.isMovable = isFabOpen
                animateFab()
            }
            R.id.fab_chat -> {
                if (currentUser == null)
                    requestProfileCreation()
                else
                    (requireActivity() as MainActivity).startChatFragment(pr?.publisher!!)
            }
            R.id.fab_favorite -> {
                if (currentUser == null)
                    requestProfileCreation()
                else
                    favoriteUserIdsUpdate()
            }
            R.id.fab_profile -> {
                if (currentUser == null)
                    requestProfileCreation()
                else
                    startProfileFragment(pr?.publisher!!)
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
            prDocumentReference = FirebaseFirestore.getInstance()
                .collection(PR_LIST)
                .document(pr?.id!!)

            updateFavoritesUi()
            setPrSnapshotListener()

            if (pr.publisherId == currentUser?.uid)
                setFabToGone()
            else
                initializeFab(fragmentView)

            fragmentView.text_view_title.text = pr.title
            fragmentView.text_view_public_name.text = pr.publisherPublicName

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
        view.fab_profile.setOnClickListener(fabClickListener)
        view.fab_chat.isMovable = false
        view.fab_favorite.isMovable = false
        view.fab_profile.isMovable = false
        view.fab_chat.scaleDown()
        view.fab_favorite.scaleDown()
        view.fab_profile.scaleDown()
        view.fab_eject.setChildButtons(view.fab_chat, view.fab_favorite, view.fab_profile)
    }

    private fun updateFavoritesUi() {
        if (pr?.favoriteUserIds!!.contains(MainActivity.currentUser?.uid)) {
            fragmentView.text_view_favorites.setBackgroundColor(ContextCompat.getColor(requireContext(),
                R.color.colorCrushedIce))
            fragmentView.fab_favorite.isEnabled = false
        } else {
            fragmentView.fab_favorite.isEnabled = true
        }
    }

    private fun setFabToGone() {
        fragmentView.fab_chat.visibility = View.GONE
        fragmentView.fab_eject.visibility = View.GONE
        fragmentView.fab_favorite.visibility = View.GONE
        fragmentView.fab_profile.visibility = View.GONE
    }

    private fun animateFab() {
        if (isFabOpen) {
            fab_eject.animate().rotation(0F).setDuration(240L).start()
            fab_chat.scaleDown()
            fab_favorite.scaleDown()
            fab_profile.scaleDown()
            fab_chat.isEnabled = false
            fab_favorite.isEnabled = false
            fab_profile.isEnabled = false
        } else {
            fab_eject.animate().rotation(45F).setDuration(240L).start()
            fab_chat.scaleUp()
            fab_favorite.scaleUp()
            fab_profile.scaleUp()
            fab_chat.isEnabled = true
            fab_favorite.isEnabled = true
            fab_profile.isEnabled = true
        }

        isFabOpen = !isFabOpen
    }

    private fun startProfileFragment(targetUser: UserModel) {
        (requireActivity() as MainActivity).startFragment(
            ProfileFragment(targetUser), R.id.frame_layout_activity_main, MainActivity.TAG_PROFILE_FRAGMENT
        )
    }

    private fun setPrSnapshotListener() {
        prListenerRegistration = prDocumentReference.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    errorHandling(e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    if (snapshot.data != null) {
                        text_view_favorites.text =
                            (snapshot.data?.get(KEY_FAVORITE_USER_IDS) as MutableList<*>)
                                .count().toString()
                    }
                } else {
                    errorHandling(NullPointerException("snapshot: null"))
                }
            }
    }

    private fun favoriteUserIdsUpdate() {
        fragmentView.text_view_favorites.isEnabled = false
        fragmentView.fab_favorite.isEnabled = false
        prDocumentReference
            .update(KEY_FAVORITE_USER_IDS,
            FieldValue.arrayUnion(currentUser?.uid!!))
            .addOnSuccessListener {
                println("$TAG: favoriteUserIds updated")
                currentUser?.favoritePrIds
                    ?.add(pr?.registrationTime.toString())
                updateFavoritesUi()
            }
            .addOnFailureListener {
                errorHandling(it, getString(R.string.failed_to_add_to_watchlist))
                fragmentView.text_view_favorites.isEnabled = true
                fragmentView.fab_favorite.isEnabled = true
            }
    }

    private fun requestProfileCreation() {
        (requireActivity() as MainActivity).requestProfileCreation()
    }

    override fun onStop() {
        removeListenerRegistration()
        super.onStop()
    }

    private fun removeListenerRegistration() {
        if(::prListenerRegistration.isInitialized)
            prListenerRegistration.remove()
    }

    private fun errorHandling(e: Exception, toastMessage: String? = null, throwing: Boolean = false) {
        errorHandler.errorHandling(e, toastMessage, throwing)
    }

    inner class ImageViewPagerAdapter(fragmentManager: FragmentManager) :
        FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getCount(): Int = youtubeVideos.count()

        override fun getItem(position: Int): Fragment = SingleImageViewFragment(youtubeVideos[position].thumbnailUri)
    }

    companion object {
        private const val TAG = "PrFragment"
    }
}