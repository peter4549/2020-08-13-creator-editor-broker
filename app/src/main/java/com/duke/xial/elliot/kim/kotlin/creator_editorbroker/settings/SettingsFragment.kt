package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.settings

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.activities.MainActivity
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.constants.FireStore.COLLECTION_USERS
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.dailog_fragments.TwoButtonDialogFragment
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.sign_in_and_sign_up.FirebaseExceptionHandler
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.showToast
import com.google.firebase.FirebaseException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_settings.view.*
import timber.log.Timber

class SettingsFragment: Fragment() {

    private lateinit var firebaseExceptionHandler: FirebaseExceptionHandler

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        firebaseExceptionHandler = FirebaseExceptionHandler(requireContext())
        initializeToolbar(view.toolbar)

        view.text_withdrawal.setOnClickListener {
            showConfirmDialog()
        }

        return view
    }

    private fun initializeToolbar(toolbar: Toolbar) {
        (requireActivity() as MainActivity).setSupportActionBar(toolbar)
        (requireActivity() as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)
    }

    private fun showConfirmDialog() {
        val dialogFragment = TwoButtonDialogFragment("탈퇴", "탈퇴하시겠습니까?")
        dialogFragment.setupFirstButton(getString(R.string.cancel)) {
            dialogFragment.dismiss()
        }
        dialogFragment.setupSecondButton(getString(R.string.ok)) {
            //deleteUserDocument(MainActivity.currentUser!!)
            deleteUser(MainActivity.currentUser?.uid!!)
            dialogFragment.dismiss()
        }
        dialogFragment.show(requireFragmentManager(), tag)
    }

    private fun deleteUser(uid: String) {
        (requireActivity() as MainActivity).firebaseAuth.currentUser?.delete()
            ?.addOnCompleteListener {
                if (it.isSuccessful) {
                    showToast(requireContext(), "탈퇴되었습니다.")
                    deleteUserDocument(uid)
                }
                else {
                    firebaseExceptionHandler.exceptionHandling(it.exception as FirebaseException)
                    // showToast(requireContext(), "오류발생. ${it.exception?.message}")
                }
            }
    }

    private fun deleteUserDocument(uid: String) {
        FirebaseFirestore.getInstance().collection(COLLECTION_USERS)
            .document(uid).delete().addOnSuccessListener {
                Timber.d("user data deleted")
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
}