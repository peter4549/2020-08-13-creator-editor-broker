package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.sign_in_and_sign_up

import android.content.Context
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.activities.MainActivity
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.clearViewsFocus
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.disableViews
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.hideKeyboard
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.showToast
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import kotlinx.android.synthetic.main.fragment_sign_up.*
import kotlinx.android.synthetic.main.fragment_sign_up.view.*
import kotlinx.android.synthetic.main.fragment_sign_up.view.button_sign_up
import kotlinx.android.synthetic.main.fragment_sign_up.view.edit_text_email
import kotlinx.android.synthetic.main.fragment_sign_up.view.edit_text_password
import kotlinx.android.synthetic.main.fragment_sign_up.view.text_input_layout_email
import kotlinx.android.synthetic.main.fragment_sign_up.view.text_input_layout_password
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timer

class SignUpFragment: Fragment() {

    private lateinit var firebaseExceptionHandler: FirebaseExceptionHandler
    private lateinit var fragmentView: View
    private lateinit var uiController: UiController
    private var timeout = TIME_OUT
    private var timer: Timer? = null
    private var resendingToken: PhoneAuthProvider.ForceResendingToken? = null
    private var verificationCode: String? = null
    private var verified = false

    private val callbacks = object :
        PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
            if (phoneAuthCredential.smsCode != null)
                verificationCode = phoneAuthCredential.smsCode
            else
                showToast(requireContext(), getString(R.string.code_lost))
        }
        override fun onVerificationFailed(e: FirebaseException) {
            firebaseExceptionHandler.exceptionHandling(e)
        }

        override fun onCodeSent(
            verificationId: String,
            forceResendingToken: PhoneAuthProvider.ForceResendingToken
        ) {
            super.onCodeSent(verificationId, forceResendingToken)
            showToast(requireContext(), getString(R.string.code_sent))
            uiController.updateUi(STATE_VERIFICATION_CODE_SENT)
            resendingToken = forceResendingToken
            text_input_layout_verification_code.error = null
            text_input_layout_verification_code.isErrorEnabled = false
            countTime()
        }
    }

    private fun countTime() {
        timer = timer(period = 1000) {
            if (timeout >= 0) {
                val minute = timeout / 60
                val seconds = timeout % 60

                --timeout

                var textSeconds = seconds.toString()

                if (seconds < 10)
                    textSeconds = "0$textSeconds"

                val text = "$minute:$textSeconds"

                requireActivity().runOnUiThread {
                    text_view_timer.text = text
                }
            } else {
                timeout = TIME_OUT
                requireActivity().runOnUiThread {
                    stopTimer()
                    text_input_layout_verification_code.isErrorEnabled = true
                    text_input_layout_verification_code.error = getString(R.string.timed_out)
                    button_send_verification_code.text = getString(R.string.send_verification_code)
                    resendingToken = null
                }
            }
        }
    }

    private fun stopTimer() {
        timer?.cancel()
        text_view_timer.text = ""
    }

    private val onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
        if (hasFocus) {
            when (view) {
                edit_text_email -> {
                    text_input_layout_email.error = null
                    text_input_layout_email.isErrorEnabled = false
                }
                edit_text_password -> {
                    text_input_layout_password.error = null
                    text_input_layout_password.isErrorEnabled = false
                }
                edit_text_phone_number -> {
                    text_input_layout_phone_number.error = null
                    text_input_layout_phone_number.isErrorEnabled = false
                }
                edit_text_verification_code -> {
                    text_input_layout_verification_code.error = null
                    text_input_layout_verification_code.isErrorEnabled = false
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentView = inflater.inflate(R.layout.fragment_sign_up, container, false)
        initializeToolbar(fragmentView.toolbar)
        uiController = UiController(fragmentView)
        firebaseExceptionHandler = FirebaseExceptionHandler(requireContext())
        firebaseExceptionHandler.setErrorFunction("ERROR_EMAIL_ALREADY_IN_USE") {
            fragmentView.text_input_layout_email.isErrorEnabled = true
            fragmentView.text_input_layout_email.error = getString(R.string.email_already_in_use)
        }
        firebaseExceptionHandler.setErrorFunction("ERROR_INVALID_EMAIL") {
            fragmentView.text_input_layout_email.isErrorEnabled = true
            fragmentView.text_input_layout_email.error = getString(R.string.invalid_email)
        }
        firebaseExceptionHandler.setErrorFunction("ERROR_WEAK_PASSWORD") {
            fragmentView.text_input_layout_password.isErrorEnabled = true
            fragmentView.text_input_layout_password.error = getString(R.string.weak_password)
        }

        fragmentView.edit_text_email.onFocusChangeListener = onFocusChangeListener
        fragmentView.edit_text_password.onFocusChangeListener = onFocusChangeListener
        fragmentView.edit_text_phone_number.onFocusChangeListener = onFocusChangeListener
        fragmentView.edit_text_verification_code.onFocusChangeListener = onFocusChangeListener

        fragmentView.button_send_verification_code.setOnClickListener {
            clearViewsFocus(edit_text_phone_number, text_input_layout_phone_number)

            val phoneNumber = fragmentView.edit_text_phone_number.text.toString()
            if (phoneNumber.isBlank())
                showToast(requireContext(), getString(R.string.please_enter_phone_number))
            else
                sendCode(phoneNumber)
        }

        fragmentView.button_ok.setOnClickListener {
            clearViewsFocus(edit_text_verification_code, text_input_layout_verification_code)

            val code = fragmentView.edit_text_verification_code.text.toString()
            if (code.isBlank())
                showToast(requireContext(), getString(R.string.please_enter_verification_code))
            else
                verifyCode(code)
        }

        fragmentView.button_sign_up.setOnClickListener {
            clearViewsFocus(edit_text_email, edit_text_password,
                edit_text_phone_number, edit_text_verification_code,
                text_input_layout_email, text_input_layout_password,
                text_input_layout_phone_number, text_input_layout_verification_code)

            if (verified)
                signUp()
            else
                showToast(requireContext(), getString(R.string.request_identity_verification))
        }

        return fragmentView
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
        CoroutineScope(Dispatchers.IO).launch {
            val countryCode = (requireContext().getSystemService(Context.TELEPHONY_SERVICE)
                    as TelephonyManager).networkCountryIso.toUpperCase(Locale.ROOT)
            try {
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    PhoneNumberUtils.formatNumberToE164(
                        phoneNumber,
                        countryCode
                    ),
                    TIME_OUT,
                    TimeUnit.SECONDS,
                    requireActivity(),
                    callbacks,
                    resendingToken
                )
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                showToast(requireContext(), getString(R.string.invalid_phone_number))
                launch(Dispatchers.Main) {
                    text_input_layout_phone_number.isErrorEnabled = true
                    text_input_layout_phone_number.error = getString(R.string.invalid_phone_number)
                }
            }
        }
    }

    private fun verifyCode(code: String) {
        if (verificationCode != null) {
            if (code == verificationCode) {
                showToast(requireContext(), getString(R.string.verified))
                verified = true
                stopTimer()
                uiController.updateUi(STATE_VERIFIED)
            } else {
                showToast(requireContext(), getString(R.string.verification_code_mismatch))
                text_input_layout_verification_code.isErrorEnabled = true
                text_input_layout_verification_code.error = getString(R.string.verification_code_mismatch)
            }
        }
    }

    private fun signUp() {
        val email = edit_text_email.text.toString()
        val password = edit_text_password.text.toString()

        if (email.isBlank()) {
            showToast(requireContext(), getString(R.string.please_enter_your_email))
            return
        }

        if (password.isBlank()) {
            showToast(requireContext(), getString(R.string.please_enter_your_password))
            return
        }

        (requireActivity() as MainActivity).firebaseAuth
            .createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Timber.d("account created")
                    hideKeyboard(requireContext(), fragmentView)
                }
                else
                    firebaseExceptionHandler.exceptionHandling(task.exception as FirebaseException)
            }
    }

    inner class UiController(private val view: View) {
        fun updateUi(state: Int) {
            when(state) {
                STATE_VERIFICATION_CODE_SENT -> {
                    button_send_verification_code.text = getString(R.string.resend)
                }
                STATE_VERIFIED -> {
                    disableViews(
                        view.button_send_verification_code, view.button_ok,
                        view.text_input_layout_phone_number, view.text_input_layout_verification_code,
                        view.edit_text_phone_number, view.edit_text_verification_code
                    )
                }
            }
        }
    }

    companion object {
        private const val STATE_VERIFICATION_CODE_SENT = 1
        private const val STATE_VERIFIED = 2
        private const val TIME_OUT = 120L
    }
}