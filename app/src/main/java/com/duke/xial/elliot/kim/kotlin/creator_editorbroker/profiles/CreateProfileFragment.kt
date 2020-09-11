package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.profiles

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.activities.MainActivity
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.activities.MainActivity.Companion.errorHandler
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.adapters.SpinnerAdapter
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.constants.FireStore.COLLECTION_USERS
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.constants.REQUEST_CODE_GALLERY
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.constants.Storage.COLLECTION_PROFILE_IMAGES
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.UserModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.setImage
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.showToast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_enter_user_information.*
import kotlinx.android.synthetic.main.fragment_enter_user_information.edit_text_email
import kotlinx.android.synthetic.main.fragment_enter_user_information.text_input_layout_email
import kotlinx.android.synthetic.main.fragment_enter_user_information.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.NullPointerException
import java.text.SimpleDateFormat
import java.util.*

class CreateProfileFragment : Fragment() {

    private lateinit var email: String
    private lateinit var publicName: String
    private var profileImageDeviceUri: Uri? = null
    private var profileImageDownloadUri: Uri? = null
    private var selectedCategories = mutableListOf<Int>()
    private var selectedUserType = 0
    private val onClickListener = View.OnClickListener {
        when(it.id) {
            R.id.image_view_profile -> openGallery()
            R.id.button_register -> {
                if (checkRequiredFields())
                    storeProfileImageAndUserProfile(profileImageDeviceUri)
            }
        }
    }

    private val onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
        if (hasFocus) {
            when (view) {
                edit_text_email -> {
                    text_input_layout_email.error = null
                    text_input_layout_email.isErrorEnabled = false
                }
                edit_text_public_name -> {
                    text_input_public_name.error = null
                    text_input_public_name.isErrorEnabled = false
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_enter_user_information, container, false)
        initializeToolbar(view.toolbar)
        initializeSpinners(view)
        view.image_view_profile.setOnClickListener(onClickListener)
        view.button_register.setOnClickListener(onClickListener)

        view.edit_text_email.onFocusChangeListener = onFocusChangeListener
        view.edit_text_public_name.onFocusChangeListener = onFocusChangeListener

        if (MainActivity.currentUser != null)
            displayExistingUserProfile(MainActivity.currentUser!!, view)

        return view
    }

    private fun initializeToolbar(toolbar: Toolbar) {
        (requireActivity() as MainActivity).setSupportActionBar(toolbar)
        (requireActivity() as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)
    }

    private fun displayExistingUserProfile(user: UserModel, view: View) {
        if (user.profileImageUri != null && user.profileImageUri != "null" &&
            user.profileImageUri?.isNotBlank() == true)
            setImage(view.image_view_profile, user.profileImageUri)
        view.edit_text_public_name.setText(user.publicName)
        view.spinner_user_type.setSelection(user.userType)
        view.spinner_category_01.setSelection(user.categories[0])
        view.spinner_category_02.setSelection(user.categories[1])
        view.edit_text_email.setText(user.email)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CODE_GALLERY -> {
                if (data != null)
                    setProfileImage(data.data!!)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> requireActivity().onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initializeSpinners(view: View) {
        view.spinner_category_01.adapter = SpinnerAdapter(requireContext(), MainActivity.contentCategories)
        view.spinner_category_02.adapter = SpinnerAdapter(requireContext(), MainActivity.contentCategories)
        view.spinner_user_type.adapter = SpinnerAdapter(requireContext(), MainActivity.userTypes)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent,  REQUEST_CODE_GALLERY)
    }

    private fun setProfileImage(uri: Uri) {
        Glide.with(image_view_profile.context)
            .load(uri)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .placeholder(R.drawable.ic_round_add_to_photos_128)
            .transition(DrawableTransitionOptions.withCrossFade())
            .transform(CenterCrop(), RoundedCorners(8))
            .into(image_view_profile)
        profileImageDeviceUri = uri
    }

    private fun checkRequiredFields(): Boolean {
        if (edit_text_public_name.text.isBlank()) {
            showToast(requireContext(), getString(R.string.enter_public_name))
            text_input_public_name.isErrorEnabled = true
            text_input_public_name.error = getString(R.string.enter_public_name)
            return false
        }

        if (spinner_user_type.selectedItem == "-") {
            showToast(requireContext(), getString(R.string.select_user_type))
            return false
        }

        if (spinner_category_01.selectedItem == "-" && spinner_category_02.selectedItem == "-") {
            showToast(requireContext(), getString(R.string.select_one_or_more_categories))
            return false
        }

        if (edit_text_email.text.isBlank()) {
            showToast(requireContext(), getString(R.string.please_enter_your_email))
            text_input_layout_email.isErrorEnabled = true
            text_input_layout_email.error = getString(R.string.please_enter_your_email)
            return false
        }

        publicName = edit_text_public_name.text.toString()
        email = edit_text_email.text.toString()
        selectedUserType = spinner_user_type.selectedItemPosition
        selectedCategories =
            setOf(spinner_category_01.selectedItemPosition, spinner_category_02.selectedItemPosition)
                .toMutableList()

        return true
    }

    private fun storeProfileImageAndUserProfile(profileImageDeviceUri: Uri?) {
        val timestamp =
            SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
        val profileImageFileName = "$timestamp.png"
        val storageReference = (requireActivity() as MainActivity)
            .firebaseAuth.currentUser?.uid?.let {
                FirebaseStorage.getInstance().reference
                    .child(COLLECTION_PROFILE_IMAGES)
                    .child(it)
                    .child(profileImageFileName)
            } ?: run {
            errorHandler
                .errorHandling(NullPointerException(
                    "failed to store user information," +
                            " FirebaseAuth.currentUser or FirebaseAuth.currentUser.uid is null"),
                    null, throwing = true)
            null
            }

        if(MainActivity.currentUser?.profileImageUri != null &&
            MainActivity.currentUser?.profileImageUri != "") {
            storageReference?.child(MainActivity.currentUser?.profileImageUri!!)?.delete()
                ?.addOnSuccessListener {
                    Timber.d("profile image deletion successful")
                }?.addOnFailureListener {
                    Timber.e(it)
                }
        }

        if (profileImageDeviceUri == null)
            storeUserProfileAfterGettingToken(null)
        else {
            storageReference?.putFile(profileImageDeviceUri)?.continueWithTask {
                return@continueWithTask storageReference.downloadUrl
            }?.addOnCompleteListener {
                if (it.isSuccessful) {
                    profileImageDownloadUri = it.result
                    println("$TAG: profile image uploaded")
                    storeUserProfileAfterGettingToken(profileImageDownloadUri)
                } else {
                    errorHandler.errorHandling(it.exception
                                ?: Exception("failed to store profile image, it.exception is null"),
                            getString(R.string.failed_to_store_profile_image)
                        )
                }
            }
        }
    }

    private fun storeUserProfileAfterGettingToken(profileImageDownloadUri: Uri?) {
        val uid = (requireActivity() as MainActivity)
            .firebaseAuth.currentUser?.uid

        if (uid == null) {
            errorHandler.errorHandling(NullPointerException("failed to store user information, uid is null"),
                    getString(R.string.uid_not_found))
            return
        }

        val user = createUser(uid)
        user.profileImageUri = profileImageDownloadUri.toString()

        CoroutineScope(Dispatchers.IO).launch {
            FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    task.result?.let { result ->
                        user.pushToken = result.token
                        storeUserProfile(user)
                    } ?: run {
                        errorHandler.errorHandling(NullPointerException("failed to get token, task.result is null"),
                                getString(R.string.failed_to_generate_token_and_regenerated_upon_sign_in))
                        storeUserProfile(user)
                    }
                } else {
                    task.exception?.let { e ->
                        errorHandler
                            .errorHandling(e, getString(R.string.failed_to_generate_token_and_regenerated_upon_sign_in))
                        storeUserProfile(user)
                    } ?: run {
                        errorHandler.errorHandling(NullPointerException("failed to get token, task.exception is null"),
                                getString(R.string.failed_to_generate_token_and_regenerated_upon_sign_in))
                        storeUserProfile(user)
                    }
                }
            }
        }
    }

    private fun createUser(uid: String) =
        UserModel(categories = selectedCategories,
            email = email,
            publicName = publicName,
            uid = uid,
            userType = selectedUserType)

    private fun storeUserProfile(user: UserModel) {
        val documentReference = FirebaseFirestore.getInstance()
            .collection(COLLECTION_USERS).document(user.uid)

        if (MainActivity.currentUser != null) {
            MainActivity.currentUser?.categories = selectedCategories
            MainActivity.currentUser?.email = email
            MainActivity.currentUser?.publicName = publicName
            MainActivity.currentUser?.userType = selectedUserType

            documentReference.update(mapOf(
                UserModel.KEY_CATEGORIES to MainActivity.currentUser?.categories,
                UserModel.KEY_EMAIL to MainActivity.currentUser?.email,
                UserModel.KEY_PUBLIC_NAME to MainActivity.currentUser?.publicName,
                UserModel.KEY_USER_TYPE to MainActivity.currentUser?.userType
            )).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast(requireContext(), getString(R.string.profile_updated))
                    requireActivity().onBackPressed()
                } else {
                    task.exception?.let { e ->
                        errorHandler.errorHandling(e, getString(R.string.failed_to_store_profile))
                    } ?: run {
                        errorHandler.errorHandling(NullPointerException("failed to store user information, task.exception is null"),
                            getString(R.string.failed_to_store_profile))
                    }
                }
            }
        }
        else {
            documentReference.set(user).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast(requireContext(), getString(R.string.profile_stored))
                    MainActivity.currentUser = user
                    requireActivity().onBackPressed()
                } else {
                    task.exception?.let { e ->
                        errorHandler.errorHandling(e, getString(R.string.failed_to_store_profile))
                    } ?: run {
                        errorHandler.errorHandling(NullPointerException("failed to store user information, task.exception is null"),
                                getString(R.string.failed_to_store_profile))
                    }
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = CreateProfileFragment()

        private const val TAG = "EnterUserInformationFragment"
    }
}