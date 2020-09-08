package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.adapters.FragmentStateAdapter
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.cloud_messaging.FirebaseMessagingService
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.constants.FireStore.COLLECTION_USERS
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.constants.HORIZONTAL
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.constants.VERTICAL
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.dailog_fragments.RequestProfileCreationDialogFragment
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.dailog_fragments.RequestSignInDialogFragment
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.error_handler.ErrorHandler
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.fragments.*
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.UserModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.UserModel.Companion.KEY_PUSH_TOKEN
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.showToast
import com.facebook.CallbackManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import timber.log.Timber
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userSnapshotListenerRegistration: ListenerRegistration
    private var selectedTabIndex = 0
    lateinit var userDocumentReference: DocumentReference
    val callbackManager: CallbackManager? = CallbackManager.Factory.create()
    val prListFragment = PrListFragment()
    val publicPartnersFragment = PublicPartnersFragment()
    val chatRoomsFragment = ChatRoomsFragment()
    val myPartnersFragment = MyPartnersFragment()

    private val tabIconResourceIds = arrayOf(
        R.drawable.ic_round_home_24,
        R.drawable.ic_round_people_24,
        R.drawable.ic_round_account_circle_24
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupTimber()
        initializeTabLayoutViewPager(tab_layout, view_pager)

        if (!errorHandlerInitialized())
            errorHandler = ErrorHandler(this)

        firebaseAuth = FirebaseAuth.getInstance()
        setAuthStateListener()

        contentCategories = createContentCategories()
        createCategoriesMap(contentCategories)

        @Suppress("DEPRECATION")
        locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            resources.configuration.locales[0]
        } else
            resources.configuration.locale

        userTypes = createUserTypes()
        userTypesMap[1] = userTypes[1]
        userTypesMap[2] = userTypes[2]
    }

    private fun setupTimber() {
        Timber.plant(Timber.DebugTree())
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.action != null) {
            when (intent.action) {
                FirebaseMessagingService.ACTION_CHAT_NOTIFICATION -> {
                    val chatRoomId =
                        intent.getStringExtra(FirebaseMessagingService.KEY_CHAT_ROOM_ID)
                    if (chatRoomId != null && chatRoomId.isNotBlank())
                        startFragment(
                            ChatFragment(existingChatRoomId = chatRoomId),
                            R.id.frame_layout_activity_main, TAG_CHAT_FRAGMENT, VERTICAL
                        )
                    else
                        errorHandler.errorHandling(
                            Exception("chat room not found"),
                            getString(R.string.chat_room_not_found)
                        )
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager?.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun initializeTabLayoutViewPager(tabLayout: TabLayout, viewPager: ViewPager2) {
        viewPager.adapter = FragmentStateAdapter(this)
        view_pager.isUserInputEnabled = false

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.tag = position
            tab.setIcon(tabIconResourceIds[position])
        }.attach()

        val linearLayout = tab_layout.getChildAt(0) as LinearLayout

        @SuppressLint("ClickableViewAccessibility")
        for (i in 0 until linearLayout.childCount) {
            linearLayout.getChildAt(i).setOnTouchListener { _, motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_UP) {
                    selectedTabIndex = i

                    when {
                        firebaseAuth.currentUser == null -> {
                            if (i != 0)
                                requestSignIn()
                        }
                        currentUser == null -> {
                            if (i != 0)
                                requestProfileCreation()
                        }
                        else -> view_pager.setCurrentItem(i, true)
                    }
                }
                true
            }
        }
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

    private fun createCategoriesMap(categories: Array<String>) {
        for ((index, category) in categories.withIndex()) {
            categoriesMap[index + 1] = category
        }
    }

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
        //readUserData()
        userDocumentReference = FirebaseFirestore.getInstance()
            .collection(COLLECTION_USERS)
            .document(firebaseAuth.currentUser?.uid.toString())
        setUserSnapshotListener()
        popAllFragments()
    }

    private val eventAfterSignOut = {
        showToast(this, getString(R.string.signed_out))
        currentUser = null
        tab_layout.getTabAt(0)?.select()
    }

    fun startSignInFragment() {
        startFragment(
            SignInFragment.newInstance(),
            R.id.frame_layout_activity_main,
            TAG_SIGN_IN_FRAGMENT
        )
    }


    private fun requestSignIn() {
        RequestSignInDialogFragment(getString(R.string.sign_in),
            getString(R.string.sign_in_request_message)).show(supportFragmentManager, TAG)
    }

    private fun requestProfileCreationAfterSignIn() {
        RequestProfileCreationDialogFragment(getString(R.string.profile_creation), getString(R.string.profile_creation_request_message_01))
            .show(supportFragmentManager, TAG)
    }

    fun requestProfileCreation() {
        RequestProfileCreationDialogFragment(getString(R.string.profile_creation), getString(R.string.profile_creation_request_message_02))
            .show(supportFragmentManager, TAG)
    }

    private fun popAllFragments() {
        while (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStackImmediate()
        }
    }

    fun startFragment(
        fragment: Fragment,
        containerViewId: Int,
        tag: String? = null,
        direction: Int = HORIZONTAL
    ) {
        when (direction) {
            HORIZONTAL -> {
                supportFragmentManager.beginTransaction()
                    .addToBackStack(null)
                    .setCustomAnimations(
                        R.anim.anim_slide_in_left,
                        R.anim.anim_slide_out_left,
                        R.anim.anim_slide_in_right,
                        R.anim.anim_slide_out_right
                    ).replace(containerViewId, fragment, tag).commit()
            }
            VERTICAL -> {
                supportFragmentManager.beginTransaction()
                    .addToBackStack(null)
                    .setCustomAnimations(
                        R.anim.anim_slide_in_bottom,
                        R.anim.anim_slide_out_top,
                        R.anim.anim_slide_in_top,
                        R.anim.anim_slide_out_bottom
                    ).replace(containerViewId, fragment, tag).commit()
            }
        }
    }

    fun startChatFragment(targetUser: UserModel) {
        startFragment(
            ChatFragment(targetUser),
            R.id.frame_layout_activity_main, TAG_CHAT_FRAGMENT
        )
    }

    fun startCreateProfileFragment() {
        startFragment(
            CreateProfileFragment.newInstance(),
            R.id.frame_layout_activity_main,
            TAG_ENTER_USER_INFORMATION_FRAGMENT
        )
    }

    fun getUserData(map: Map<String, Any>): UserModel =
        Gson().fromJson(JSONObject(map).toString(), UserModel::class.java)

    override fun onPause() {
        if (currentUser != null)
            updateChangedUserInformation()
        super.onPause()
    }

    override fun onStop() {
        prListFragment.removePrSnapshotListener()
        if (::userSnapshotListenerRegistration.isInitialized)
            userSnapshotListenerRegistration.remove()
        chatRoomsFragment.removeListenerRegistration()
        super.onStop()
    }

    private fun updateChangedUserInformation() {
        val map = mutableMapOf<String, Any>()

        if (ChangedData.channelIdsChanged)
            map[UserModel.KEY_CHANNEL_IDS] = currentUser!!.channelIds

        if (ChangedData.chatRoomsChanged) /** 챗룸정보 불필요할 수 있음.*/
            map[UserModel.KEY_CHAT_ROOM_IDS] = currentUser!!.chatRoomIds

        if (ChangedData.prListChanged)
            map[UserModel.KEY_MY_PR_IDS] = currentUser!!.myPrIds

        if (map.isNotEmpty()) {
            @Suppress("UNCHECKED_CAST")
            userDocumentReference
                .update(map)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        println("$TAG: changed user information updated")
                    } else
                        errorHandler.errorHandling(
                            task.exception
                                ?: NullPointerException("failed to update user information")
                        )

                    ChangedData.channelIdsChanged = false
                    ChangedData.chatRoomsChanged = false
                    ChangedData.prListChanged = false
                }
        }
    }

    private fun setUserSnapshotListener() {
        userSnapshotListenerRegistration = userDocumentReference.addSnapshotListener { snapshot, e ->
            if (e != null)
                errorHandler.errorHandling(e)
            else {
                if (snapshot != null && snapshot.exists()) {
                    showToast(this, "SOMEUPDATE")
                    currentUser = getUserData(snapshot.data!!) // 이거 완료 전까지 다른 프래그먼트 터치 봉인할것.
                    if (intent != null) {
                        if (intent.action == "action.ad.astra.cloud.message.click") {
                            val chatRoomId = intent.extras?.get("roomId") as String?
                            if (chatRoomId != null) {
                                startFragment(
                                    ChatFragment(existingChatRoomId = chatRoomId),
                                    R.id.frame_layout_activity_main, TAG_CHAT_FRAGMENT, VERTICAL
                                )
                            } else
                                showToast(this, getString(R.string.chat_room_not_found))
                        }
                    }
                } else {
                    requestProfileCreationAfterSignIn()
                    println("$TAG: data is null")
                }
            }
        }
    }

    private fun createToken() { // 생성이 아닌 참조. 굳이 실행할 필요가 없다.
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val pushToken = task.result?.token
                if (pushToken != null) {
                    val map = mapOf(
                        KEY_PUSH_TOKEN to pushToken
                    )

                    currentUser?.pushToken = pushToken

                    FirebaseFirestore.getInstance()
                        .collection(COLLECTION_USERS)
                        .document(firebaseAuth.currentUser?.uid.toString())
                        .update(map).addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful)
                                println("$TAG: token updated")
                            else
                                errorHandler.errorHandling(task.exception as FirebaseFirestoreException)
                        }
                } else
                    errorHandler.errorHandling(
                        NullPointerException("token generation failed"),
                        getString(R.string.token_generation_failed)
                    )
            } else
                errorHandler.errorHandling(
                    task.exception!!,
                    getString(R.string.token_generation_failed)
                )
        }
    }

    object ChangedData {
        var channelIdsChanged = false
        var chatRoomsChanged = false /** 챗룸정보 불필요할 수 있음.*/
        var prListChanged = false
    }

    companion object {
        const val TAG_CHAT_FRAGMENT = "tag_chat_fragment"
        const val TAG_ENTER_USER_INFORMATION_FRAGMENT = "tag_enter_user_information_fragment"
        const val TAG_PR_FRAGMENT = "tag_pr_fragment"
        const val TAG_PROFILE_FRAGMENT = "tag_profile_fragment"
        const val TAG_SIGN_IN_FRAGMENT = "tag_sign_in_fragment"
        const val TAG_SIGN_UP_FRAGMENT = "tag_sign_up_fragment"
        const val TAG_WRITE_PR_FRAGMENT = "tag_write_pr_fragment"
        lateinit var contentCategories: Array<String>
        lateinit var errorHandler: ErrorHandler
        lateinit var locale: Locale
        lateinit var userTypes: Array<String>
        private const val TAG = "MainActivity"
        val categoriesMap = mutableMapOf<Int, String>()
        val userTypesMap = mutableMapOf<Int, String>()
        var currentUser: UserModel? = null
        var currentChatRoomId: String? = null

        fun errorHandlerInitialized() = ::errorHandler.isInitialized
    }
}