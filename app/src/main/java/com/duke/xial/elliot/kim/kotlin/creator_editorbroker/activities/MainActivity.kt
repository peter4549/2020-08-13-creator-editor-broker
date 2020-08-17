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
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.CommentModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.Tier
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.UserInformationModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.VideoModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.showToast
import com.facebook.CallbackManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    lateinit var firebaseAuth: FirebaseAuth
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

    fun startFragment(fragment: Fragment, containerViewId: Int, tag: String? = null) {
        supportFragmentManager.beginTransaction()
            .addToBackStack(null)
            .setCustomAnimations(
                R.anim.anim_slide_in_left,
                R.anim.anim_slide_out_left,
                R.anim.anim_slide_in_right,
                R.anim.anim_slide_out_right
            ).replace(containerViewId, fragment, tag).commit()
    }

    private fun readUserData() {
        FirebaseFirestore.getInstance()
            .collection(FireStore.Collection.COLLECTION_USERS)
            .document(firebaseAuth.currentUser?.uid.toString())
            .get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result != null)
                        if (task.result?.data == null) {
                            currentUserInformation = null
                            startEnterUserInformationFragment()
                        }
                        else {
                            setCurrentUserData(task.result?.data as Map<String, Any>)
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
        startFragment(EnterUserInformationFragment(),
            R.id.constraint_layout_activity_main,
            TAG_ENTER_USER_INFORMATION_FRAGMENT)
    }

    private fun setCurrentUserData(map: Map<String, Any>) {
        println("AAAAAAA: $map")
        currentUserInformation
    }
//
    var categories: MutableList<Int>,
    var publicName: String,
    var uid: String,
    var userType: Int,
    var channelIds: MutableList<String> = mutableListOf(),
    var commentsWritten: MutableList<CommentModel> = mutableListOf(),
    var favoritePrIds: MutableList<String> = mutableListOf(),
    var favoriteUserIds: MutableList<String> = mutableListOf(),
    var myPrIds: MutableList<String> = mutableListOf(),
    var partnerIds: MutableList<String> = mutableListOf(),
    var profileImageUri: String? = null,
    var pushToken: String? = null,
    var receivedStarCount: Int = 0,
    var registeredOnPartners: Boolean = false,
    var tier: Int = Tier.NORMAL,
    var userIdsReceivedMyStar: MutableList<String> = mutableListOf(),
    var youtubeVideos: MutableList<VideoModel> = mutableListOf())
//
    @Suppress("UNCHECKED_CAST")
    private fun createUserInformationFromMap(map: Map<String, Any>): UserInformationModel {
        return UserInformationModel(categories = map[UserInformationModel.KEY_CATEGORIES] as MutableList<Int>,
            publicName = map[UserInformationModel.KEY_PUBLIC_NAME] as String,
            uid = map[UserInformationModel.KEY_UID] as String,
            userType = map[UserInformationModel.KEY_USER_TYPE] as Int,
            channelIds = map[UserInformationModel.KEY_CHANNEL_IDS] as MutableList<String>,
            commentsWritten = map[UserInformationModel.KEY_COMMENTS_WRITTEN] as MutableList<CommentModel>,
            favoritePrIds = map[UserInformationModel.KEY_FAVORITE_PR_IDS] as MutableList<String>,
            favoriteUserIds = map[UserInformationModel.KEY_FAVORITE_USER_IDS] as MutableList<String>,
            myPrIds = map[UserInformationModel.KEY_PUBLIC_NAME] as String,
            partnerIds = map[UserInformationModel.KEY_PUBLIC_NAME] as String,
            profileImageUri = map[UserInformationModel.KEY_PUBLIC_NAME] as String,
            pushToken = map[UserInformationModel.KEY_PUBLIC_NAME] as String,
            receivedStarCount = map[UserInformationModel.KEY_PUBLIC_NAME] as String,
            registeredOnPartners = map[UserInformationModel.KEY_PUBLIC_NAME] as String,
            tier = map[UserInformationModel.KEY_PUBLIC_NAME] as String,
            userIdsReceivedMyStar = map[UserInformationModel.KEY_PUBLIC_NAME] as String,
            youtubeVideos = map[UserInformationModel.KEY_PUBLIC_NAME] as String
        )
    }

    companion object {
        const val TAG_ENTER_USER_INFORMATION_FRAGMENT = "tag_enter_user_information_fragment"
        const val TAG_SIGN_IN_FRAGMENT = "tag_sign_in_fragment"

        lateinit var contentCategories: Array<String>
        lateinit var userTypes: Array<String>

        var currentUserInformation: UserInformationModel? = null
    }
}