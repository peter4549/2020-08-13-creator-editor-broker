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
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.activities.MainActivity
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.constants.REQUEST_CODE_GALLERY
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.constants.Storage.Collection.COLLECTION_PROFILE_IMAGES
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.models.UserInformationModel
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_enter_user_information.*
import java.lang.NullPointerException
import java.text.SimpleDateFormat
import java.util.*

class EnterUserInformationFragment : Fragment() {

    private var profileImageDeviceUri: Uri? = null
    private var profileImageFileDownloadUri: Uri? = null
    private var userInformationRegistered = false  // 이거 왜 달앗지.. 수정동작이랑 구분할라고?
    private val onClickListener = View.OnClickListener {
        when(it.id) {
            R.id.image_view_profile -> openGallery()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_enter_user_information, container, false)
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
            .placeholder(R.drawable.ic_baseline_add_to_photos_128)
            .transition(DrawableTransitionOptions.withCrossFade())
            .transform(CircleCrop(), RoundedCorners(8))
            .into(image_view_profile)
        profileImageDeviceUri = uri
    }

    private fun checkRequiredFields() {

    }

    private fun storeData(profileImageDeviceUri: Uri?) {
        // 빈칸검사로직.
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
            "실제 저장 로직...."
        else {
            storageReference?.putFile(profileImageDeviceUri)?.continueWithTask {
                return@continueWithTask storageReference.downloadUrl
            }?.addOnCompleteListener {
                if (it.isSuccessful) {
                    profileImageFileDownloadUri = it.result
                    println("$TAG: profile image uploaded")
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

    private fun storeUserInformation(uri: Uri?) {

    }

    private fun createUserInformationModel(uid: String) =
        UserInformationModel(categories = ,
            publicName = edit_text_public_name.text.toString(),
            uid = uid,
            userType = )


    companion object {
        @JvmStatic
        fun newInstance() = EnterUserInformationFragment()

        private const val TAG = "EnterUserInformationFragment"
    }
}