package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.adapters.FragmentStateAdapter
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.constants.FireStore
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.error_handler.ErrorHandler
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.fragments.EnterUserInformationFragment
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.fragments.PartnersFragment
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.fragments.PrListFragment
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.fragments.SignInFragment
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.UserInformationModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.showToast
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.youtube.YouTubeChannelsActivity
import com.facebook.CallbackManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.lang.NullPointerException

class MainActivity : AppCompatActivity() {
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var fireStoreDocumentReference: DocumentReference
    val callbackManager: CallbackManager? = CallbackManager.Factory.create()
    val errorHandler = ErrorHandler(this)
    val prListFragment = PrListFragment()
    val partnersFragment = PartnersFragment()

    private val tabIconResourceIds = arrayOf(
        R.drawable.ic_round_home_24,
        R.drawable.ic_round_people_24,
        R.drawable.ic_round_account_circle_24
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initializeTabLayoutViewPager(tab_layout, view_pager)

        firebaseAuth = FirebaseAuth.getInstance()
        setAuthStateListener()

        contentCategories = createContentCategories()
        userTypes = createUserTypes()

        startFragment(SignInFragment(), R.id.constraint_layout_activity_main, TAG_SIGN_IN_FRAGMENT)
    }

    override fun onStart() {
        super.onStart()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager?.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun initializeTabLayoutViewPager(tabLayout: TabLayout, viewPager: ViewPager2) {
        viewPager.adapter = FragmentStateAdapter(this)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.tag = position
            tab.setIcon(tabIconResourceIds[position])
        }.attach()
    }

    private fun createContentCategories(): Array<String> = arrayOf(
        "-",
        getString(R.string.category_car),
        getString(R.string.category_beauty_fashion),
        getString(R.string.category_comedy),
        getString(R.string.category_education),
        getString(R.string.category_entertainment),
        getString(R.string.category_family_entertainment),
        getString(R.string.category_movie_animation),
        getString(R.string.category_food),
        getString(R.string.category_game),
        getString(R.string.category_know_how_style),
        getString(R.string.category_music),
        getString(R.string.category_news_politics),
        getString(R.string.category_non_profit_social_movement),
        getString(R.string.category_people_blog),
        getString(R.string.category_pets_animals),
        getString(R.string.category_science_technology),
        getString(R.string.category_sports),
        getString(R.string.category_travel_event)
    )

    private fun createUserTypes(): Array<String> = arrayOf(
        "-",
        getString(R.string.creator),
        getString(R.string.editor)
    )

    private fun setAuthStateListener() {
        firebaseAuth.addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null)
                eventAfterSignIn()
            else
                eventAfterSignOut()
        }
    }

    private val eventAfterSignIn = {
        showToast(this, getString(R.string.signed_in))
        popAllFragments()
        readUserData()
    }

    private val eventAfterSignOut = {
        showToast(this, getString(R.string.signed_out))
        currentUserInformation = null
    }

    private fun popAllFragments() {
        while (supportFragmentManager.backStackEntryCount > 0)
            supportFragmentManager.popBackStackImmediate()
    }

    fun startFragment(fragment: Fragment, containerViewId: Int, tag: String? = null, direction: Int = HORIZONTAL) {
        when (direction) {
            HORIZONTAL -> supportFragmentManager.beginTransaction()
                .addToBackStack(null)
                .setCustomAnimations(
                    R.anim.anim_slide_in_left,
                    R.anim.anim_slide_out_left,
                    R.anim.anim_slide_in_right,
                    R.anim.anim_slide_out_right
                ).replace(containerViewId, fragment, tag).commit()
            VERTICAL -> supportFragmentManager.beginTransaction()
                .addToBackStack(null)
                .setCustomAnimations(
                    R.anim.anim_slide_in_bottom,
                    R.anim.anim_slide_out_top,
                    R.anim.anim_slide_in_top,
                    R.anim.anim_slide_out_bottom
                ).replace(containerViewId, fragment, tag).commit()
        }
    }

    private fun readUserData() {
        fireStoreDocumentReference = FirebaseFirestore.getInstance()
            .collection(FireStore.COLLECTION_USERS)
            .document(firebaseAuth.currentUser?.uid.toString())
        fireStoreDocumentReference.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result != null)
                        if (task.result?.data == null) {
                            currentUserInformation = null
                            startEnterUserInformationFragment()
                        }
                        else {
                            currentUserInformation = getCurrentUserData(task.result?.data as Map<String, Any>)
                        }
                    else
                        errorHandler.errorHandling(
                            Exception("failed to read user data, task.result is null"),
                            getString(R.string.failed_to_read_user_data))
                } else
                    errorHandler.errorHandling(task.exception ?:
                    Exception("failed to read user data, task.exception is null"),
                        getString(R.string.failed_to_read_user_data))
            }
    }

    private fun startEnterUserInformationFragment() {
        startFragment(EnterUserInformationFragment.newInstance(),
            R.id.constraint_layout_activity_main,
            TAG_ENTER_USER_INFORMATION_FRAGMENT)
    }

    private fun getCurrentUserData(map: Map<String, Any>): UserInformationModel =
        Gson().fromJson(JSONObject(map).toString(), UserInformationModel::class.java)

    override fun onPause() {
        if (currentUserInformation != null)
            updateChangedUserInformation()
        super.onPause()
    }

    private fun updateChangedUserInformation() {
        val map = mutableMapOf<String, Any>()

        if (channelIdsChanged)
            map[UserInformationModel.KEY_CHANNEL_IDS] = currentUserInformation!!.channelIds

        if (map.isNotEmpty()) {
            @Suppress("UNCHECKED_CAST")
            fireStoreDocumentReference
                .update(map)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        println("$TAG: changed user information updated")
                    } else
                        errorHandler.errorHandling(
                            task.exception
                                ?: NullPointerException("failed to update user information")
                        )
                }
        }
    }

    companion object {
        const val HORIZONTAL = 0
        const val VERTICAL = 1
        const val TAG_ENTER_USER_INFORMATION_FRAGMENT = "tag_enter_user_information_fragment"
        const val TAG_SIGN_IN_FRAGMENT = "tag_sign_in_fragment"
        const val TAG_WRITE_PR_FRAGMENT = "tag_write_pr_fragment"

        lateinit var contentCategories: Array<String>
        lateinit var userTypes: Array<String>
        private const val TAG = "MainActivity"
        var currentUserInformation: UserInformationModel? = null
        var channelIdsChanged = false
    }
}