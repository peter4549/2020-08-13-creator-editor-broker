package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.fragments

import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.activities.MainActivity
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.showToast
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import kotlinx.android.synthetic.main.fragment_sign_up.*
import kotlinx.android.synthetic.main.fragment_sign_up.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

class SignUpFragment: Fragment() {
    private lateinit var uiController: UiController
    private val timeout = 60L
    private var resendingToken: PhoneAuthProvider.ForceResendingToken? = null
    private var verificationCode: String? = null

    private val callbacks = object :
        PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
            if (phoneAuthCredential.smsCode != null)
                verificationCode = phoneAuthCredential.smsCode
            else
                showToast(requireContext(), getString(R.string.code_lost))
        }
        override fun onVerificationFailed(e: FirebaseException) {
            (requireActivity() as MainActivity).errorHandler.errorHandling(e)
        }

        override fun onCodeSent(verificationId: String, forceResendingToken: PhoneAuthProvider.ForceResendingToken) {
            super.onCodeSent(verificationId, forceResendingToken)
            showToast(requireContext(), getString(R.string.code_sent))
            uiController.updateUi(STATE_VERIFICATION_CODE_SENT)
            resendingToken = forceResendingToken
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sign_up, container, false)
        initializeToolbar(view.toolbar)
        uiController = UiController(view)
        uiController.updateUi(STATE_INITIALIZED)

        view.button_send.setOnClickListener {
            val phoneNumber = view.edit_text_phone_number.text.toString()
            if (phoneNumber.isBlank())
                showToast(requireContext(), getString(R.string.please_enter_phone_number))
            else
                sendCode(phoneNumber)
        }

        view.button_verification.setOnClickListener {
            val code = view.edit_text_verification_code.text.toString()
            if (code.isBlank())
                showToast(requireContext(), getString(R.string.please_enter_verification_code))
            else
                verifyCode(code)
        }

        view.button_sign_up.setOnClickListener {
            signUp()
        }

        return view
    }

    private fun initializeToolbar(toolbar: Toolbar) {
        (requireActivity() as MainActivity).setSupportActionBar(toolbar)
        (requireActivity() as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)
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

    private fun sendCode(phoneNumber: String) {
        println("BBBBBBBB" + Locale.getDefault().displayCountry.toString()) // 이걸로 스피너 구성?? 나라이름임.
        println("CCCCCCCC" + Locale.getDefault().isO3Country.toString())  // ios3 form,, maybe not used?
        println("OOOOOOOO" + Locale.getDefault().country) // 이게 정담. 예를 넣어야 올바른 국가코드 전번 생성.

        for(i in Locale.getISOCountries().withIndex()) {  // 1: KR 이런식으로 출력.
            println(""+ i.index + ":" + i.value)
            //Locale
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    PhoneNumberUtils.formatNumberToE164(phoneNumber, Locale.getDefault().country),
                    timeout,
                    TimeUnit.SECONDS,
                    requireActivity(),
                    callbacks
                )
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                showToast(requireContext(), getString(R.string.invalid_country_code))
            }
        }
    }

    private fun verifyCode(code: String) {
        if (verificationCode != null) {
            if (code == verificationCode) {
                showToast(requireContext(), getString(R.string.verified))
                uiController.updateUi(STATE_VERIFIED)
            } else {
                showToast(requireContext(), getString(R.string.verification_code_dose_not_match))
            }
        }
    }

    private fun signUp() {
        val email = edit_text_email.text.toString()
        val password = edit_text_password.text.toString()

        if (email.isBlank()) {
            showToast(requireContext(), getString(R.string.please_enter_email))
            return
        }

        if (password.isBlank()) {
            showToast(requireContext(), getString(R.string.please_enter_password))
            return
        }

        (requireActivity() as MainActivity).firebaseAuth
            .createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful)
                    println("$TAG: account created")
                else
                    showExceptionMessage(task)
            }
    }

    private fun showExceptionMessage(task: Task<AuthResult>) {
        try {
            throw task.exception!!
        } catch (e: FirebaseAuthWeakPasswordException) {
            showToast(requireContext(), getString(R.string.password_too_weak))
            edit_text_password.requestFocus()
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            showToast(requireContext(), getString(R.string.invalid_email))
            edit_text_email.requestFocus()
        } catch (e: FirebaseAuthUserCollisionException) {
            showToast(requireContext(), getString(R.string.email_already_signed_up))
            edit_text_email.requestFocus()
        } catch (e: Exception) {
            showToast(requireContext(), getString(R.string.failed_to_create_account))
        }
    }

    inner class UiController(private val view: View) {
        fun updateUi(state: Int) {
            when(state) {
                STATE_INITIALIZED -> {
                    disableViews(view.button_sign_up)
                }
                STATE_VERIFICATION_CODE_SENT -> {
                    button_send.text = getString(R.string.resend)
                }
                STATE_VERIFIED -> {
                    disableViews(view.button_send, view.button_verification,
                        view.edit_text_phone_number, view.edit_text_verification_code)
                    enableViews(view.button_sign_up)
                }
            }
        }
    }

    private fun enableViews(vararg views: View) {
        for (v in views) {
            v.isEnabled = true
        }
    }

    private fun disableViews(vararg views: View) {
        for (v in views) {
            v.isEnabled = false
        }
    }

    companion object {
        private const val STATE_INITIALIZED = 0
        private const val STATE_VERIFICATION_CODE_SENT = 1
        private const val STATE_VERIFIED = 2
        private const val STATE_VERIFICATION_FAILED = 3
        private const val TAG = "SignUpFragment"
    }
}