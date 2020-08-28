package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.adapters.FragmentStateAdapter
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.cloud_messaging.FirebaseMessagingService
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.constants.FireStore.COLLECTION_USERS
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.constants.HORIZONTAL
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.constants.VERTICAL
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.error_handler.ErrorHandler
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.fragments.*
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.UserInformationModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.UserInformationModel.Companion.KEY_PUSH_TOKEN
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.showToast
import com.facebook.CallbackManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.lang.NullPointerException
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var firebaseAuth: FirebaseAuth
    private lateinit var fireStoreDocumentReference: DocumentReference
    private lateinit var userSnapshotListenerRegistration: ListenerRegistration
    private lateinit var userDocumentReference: DocumentReference
    val callbackManager: CallbackManager? = CallbackManager.Factory.create()
    val errorHandler = ErrorHandler(this)
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
        initializeTabLayoutViewPager(tab_layout, view_pager)

        firebaseAuth = FirebaseAuth.getInstance()
        setAuthStateListener()

        contentCategories = createContentCategories()

        @Suppress("DEPRECATION")
        locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            resources.configuration.locales[0]
        } else
            resources.configuration.locale

        println("LLLLLLLL + $locale")
        println("DEFAULTLOCALE" + Locale.getDefault().country)
        println("DEFAULTLOCALE" + Locale.getDefault().toLanguageTag())
        println("DEFAULTLOCALE" + Locale.getDefault().displayCountry)
        println("DEFAULTLOCALE" + Locale.getDefault().script)
        println("AVLOCALE" + Locale.getAvailableLocales())
        println("IOSCOUNTRY" + Locale.getISOCountries())
        println("IOSLAN" + Locale.getISOLanguages())

        userTypes = createUserTypes()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.action != null) {
            when (intent.action) {
                FirebaseMessagingService.ACTION_CHAT_NOTIFICATION -> {
                    val chatRoomId = intent.getStringExtra(FirebaseMessagingService.KEY_CHAT_ROOM_ID)
                    if (chatRoomId != null && chatRoomId.isNotBlank())
                        startFragment(
                            ChatFragment(existingChatRoomId = chatRoomId),
                            R.id.frame_layout_activity_main, TAG_CHAT_FRAGMENT, VERTICAL
                        )
                    else
                        errorHandler.errorHandling(Exception("chat room not found"),
                            getString(R.string.chat_room_not_found))
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
        //readUserData()
        fireStoreDocumentReference = FirebaseFirestore.getInstance()
            .collection(COLLECTION_USERS)
            .document(firebaseAuth.currentUser?.uid.toString())
        setUserDocumentListener(firebaseAuth.uid.toString())
        popAllFragments()
    }

    private val eventAfterSignOut = {
        showToast(this, getString(R.string.signed_out))
        currentUserInformation = null
        startFragment(SignInFragment.newInstance(), R.id.frame_layout_activity_main, TAG_SIGN_IN_FRAGMENT)
    }

    private fun popAllFragments() {
        while (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStackImmediate()
        }
    }

    fun startFragment(fragment: Fragment, containerViewId: Int, tag: String? = null, direction: Int = HORIZONTAL) {
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

    /*
    private fun readUserData() {
        fireStoreDocumentReference = FirebaseFirestore.getInstance()
            .collection(COLLECTION_USERS)
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
                            setUserDocumentListener(currentUserInformation!!.uid)
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

     */

    private fun startEnterUserInformationFragment() {
        println("HAHAHAHAHAHAHHAHHAHAHAH")// 프래그먼트 commit 에러 조사.
        startFragment(EnterUserInformationFragment.newInstance(),
            R.id.frame_layout_activity_main,
            TAG_ENTER_USER_INFORMATION_FRAGMENT)
    }

    fun getUserData(map: Map<String, Any>): UserInformationModel =
        Gson().fromJson(JSONObject(map).toString(), UserInformationModel::class.java)

    override fun onPause() {
        if (currentUserInformation != null)
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
            map[UserInformationModel.KEY_CHANNEL_IDS] = currentUserInformation!!.channelIds

        if (ChangedData.chatRoomsChanged) /** 챗룸정보 불필요할 수 있음.*/
            map[UserInformationModel.KEY_CHAT_ROOM_IDS] = currentUserInformation!!.chatRoomIds

        if (ChangedData.prListChanged)
            map[UserInformationModel.KEY_MY_PR_IDS] = currentUserInformation!!.myPrIds

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

                    ChangedData.channelIdsChanged = false
                    ChangedData.chatRoomsChanged = false
                    ChangedData.prListChanged = false
                }
        }
    }

    private fun setUserDocumentListener(uid: String) {
        userDocumentReference = FirebaseFirestore.getInstance()
            .collection(COLLECTION_USERS).document(uid)
        userSnapshotListenerRegistration = userDocumentReference.addSnapshotListener { snapshot, e ->
            if (e != null)
                errorHandler.errorHandling(e)
            else {
                if (snapshot != null && snapshot.exists()) {
                    currentUserInformation = getUserData(snapshot.data!!) // 이거 완료 전까지 다른 프래그먼트 터치 봉인할것.
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
                    startEnterUserInformationFragment()
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

                    currentUserInformation?.pushToken = pushToken

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
                    errorHandler.errorHandling(NullPointerException("token generation failed"),
                        getString(R.string.token_generation_failed))
            } else
                errorHandler.errorHandling(task.exception!!, getString(R.string.token_generation_failed))
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
        const val TAG_SIGN_IN_FRAGMENT = "tag_sign_in_fragment"
        const val TAG_SIGN_UP_FRAGMENT = "tag_sign_up_fragment"
        const val TAG_WRITE_PR_FRAGMENT = "tag_write_pr_fragment"
        lateinit var contentCategories: Array<String>
        lateinit var locale: Locale
        lateinit var userTypes: Array<String>
        private const val TAG = "MainActivity"
        var currentUserInformation: UserInformationModel? = null
        var currentChatRoomId: String? = null
    }
}