package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.activities.MainActivity
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.adapters.SpinnerAdapter
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.constants.FireStore.Collection.COLLECTION_USERS
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.constants.REQUEST_CODE_GALLERY
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.constants.Storage.Collection.COLLECTION_PROFILE_IMAGES
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.UserInformationModel
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.showToast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_enter_user_information.*
import kotlinx.android.synthetic.main.fragment_enter_user_information.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.NullPointerException
import java.text.SimpleDateFormat
import java.util.*

class EnterUserInformationFragment : Fragment() {

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
                    storeData(profileImageDeviceUri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_enter_user_information, container, false)
        initializeSpinners(view)
        view.image_view_profile.setOnClickListener(onClickListener)
        view.button_register.setOnClickListener(onClickListener)

        return view
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

        publicName = edit_text_public_name.text.toString()
        selectedUserType = spinner_user_type.selectedItemPosition
        selectedCategories =
            setOf(spinner_category_01.selectedItemPosition, spinner_category_02.selectedItemPosition)
                .toMutableList()

        return true
    }

    private fun storeData(profileImageDeviceUri: Uri?) {
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
            (requireActivity() as MainActivity).errorHandler
                .errorHandling(NullPointerException(
                    "failed to store user information," +
                            " FirebaseAuth.currentUser or FirebaseAuth.currentUser.uid is null"),
                    null, throwing = true)
            null
            }

        if (profileImageDeviceUri == null)
            storeUserInformation(null)
        else {
            storageReference?.putFile(profileImageDeviceUri)?.continueWithTask {
                return@continueWithTask storageReference.downloadUrl
            }?.addOnCompleteListener {
                if (it.isSuccessful) {
                    profileImageDownloadUri = it.result
                    println("$TAG: profile image uploaded")
                    storeUserInformation(profileImageDownloadUri)
                } else {
                    (requireActivity() as MainActivity).errorHandler
                        .errorHandling(
                            it.exception
                                ?: Exception("failed to store profile image, it.exception is null"),
                            getString(R.string.failed_to_store_profile_image)
                        )
                }
            }
        }
    }

    private fun storeUserInformation(profileImageDownloadUri: Uri?) {
        val uid = (requireActivity() as MainActivity)
            .firebaseAuth.currentUser?.uid

        if (uid == null) {
            (requireActivity() as MainActivity).errorHandler
                .errorHandling(NullPointerException("failed to store user information, uid is null"),
                    getString(R.string.uid_not_found))
            return
        }

        val userInformation = createUserInformationModel(uid)
        userInformation.profileImageUri = profileImageDownloadUri.toString()

        CoroutineScope(Dispatchers.IO).launch {
            FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    task.result?.let { result ->
                        userInformation.pushToken = result.token
                        setUserInformationInDocument(userInformation)
                    } ?: run {
                        (requireActivity() as MainActivity).errorHandler
                            .errorHandling(NullPointerException("failed to get token, task.result is null"),
                                getString(R.string.failed_to_generate_token_and_regenerated_upon_sign_in))
                        setUserInformationInDocument(userInformation)
                    }
                } else {
                    task.exception?.let { e ->
                        (requireActivity() as MainActivity).errorHandler
                            .errorHandling(e, getString(R.string.failed_to_generate_token_and_regenerated_upon_sign_in))
                        setUserInformationInDocument(userInformation)
                    } ?: run {
                        (requireActivity() as MainActivity).errorHandler
                            .errorHandling(NullPointerException("failed to get token, task.exception is null"),
                                getString(R.string.failed_to_generate_token_and_regenerated_upon_sign_in))
                        setUserInformationInDocument(userInformation)
                    }
                }
            }
        }
    }

    private fun createUserInformationModel(uid: String) =
        UserInformationModel(categories = selectedCategories,
            publicName = publicName,
            uid = uid,
            userType = selectedUserType)

    private fun setUserInformationInDocument(userInformation: UserInformationModel) {
        val documentReference = FirebaseFirestore.getInstance()
            .collection(COLLECTION_USERS).document(userInformation.uid)

        if (MainActivity.currentUserInformation != null)
            "프로필 업데이트"
        else {
            documentReference.set(userInformation).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast(requireContext(), getString(R.string.profile_stored))
                    MainActivity.currentUserInformation = userInformation
                } else {
                    task.exception?.let { e ->
                        (requireActivity() as MainActivity).errorHandler
                            .errorHandling(e, getString(R.string.failed_to_store_profile))
                    } ?: run {
                        (requireActivity() as MainActivity).errorHandler
                            .errorHandling(NullPointerException("failed to store user information, task.exception is null"),
                                getString(R.string.failed_to_store_profile))
                    }
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = EnterUserInformationFragment()

        private const val TAG = "EnterUserInformationFragment"
    }
}