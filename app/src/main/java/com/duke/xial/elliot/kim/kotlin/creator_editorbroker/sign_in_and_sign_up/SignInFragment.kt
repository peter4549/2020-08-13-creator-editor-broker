package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.sign_in_and_sign_up

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.activities.MainActivity
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.constants.REQUEST_CODE_GOOGLE_SIGN_IN
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.hideKeyboard
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.showToast
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginBehavior
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import kotlinx.android.synthetic.main.fragment_sign_in.*
import kotlinx.android.synthetic.main.fragment_sign_in.view.*
import timber.log.Timber

class SignInFragment : Fragment() {

    private lateinit var firebaseExceptionHandler: FirebaseExceptionHandler
    private val buttonClickListener = View.OnClickListener { view ->
        hideKeyboard(requireContext(), view)
        when(view.id) {
            R.id.button_sign_in_with_email -> signInWithEmail()
            R.id.button_sign_in_with_google -> signInWithGoogle()
            R.id.button_sign_in_with_facebook -> signInWithFacebook()
            R.id.button_sign_in_with_twitter -> signInWithTwitter()
            R.id.button_sign_up -> startSignUpFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sign_in, container, false)
        view.button_sign_in_with_email.setOnClickListener(buttonClickListener)
        view.button_sign_in_with_google.setOnClickListener(buttonClickListener)
        view.button_sign_in_with_facebook.setOnClickListener(buttonClickListener)
        view.button_sign_in_with_twitter.setOnClickListener(buttonClickListener)
        view.button_sign_up.setOnClickListener(buttonClickListener)

        view.edit_text_email.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                view.text_input_layout_email.error = null
                view.text_input_layout_email.isErrorEnabled = false
            }
        }

        view.edit_text_password.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                view.text_input_layout_password.error = null
                view.text_input_layout_password.isErrorEnabled = false
            }
        }

        firebaseExceptionHandler = FirebaseExceptionHandler(requireContext())
        firebaseExceptionHandler.setErrorFunction("ERROR_EMAIL_ALREADY_IN_USE") {
            view.text_input_layout_email.isErrorEnabled = true
            view.text_input_layout_email.error = getString(R.string.email_already_in_use)
        }
        firebaseExceptionHandler.setErrorFunction("ERROR_INVALID_EMAIL") {
            view.text_input_layout_email.isErrorEnabled = true
            view.text_input_layout_email.error = getString(R.string.invalid_email)
        }
        firebaseExceptionHandler.setErrorFunction("ERROR_USER_NOT_FOUND") {
            view.text_input_layout_email.isErrorEnabled = true
            view.text_input_layout_email.error = getString(R.string.user_not_found)
        }
        firebaseExceptionHandler.setErrorFunction("ERROR_WRONG_PASSWORD") {
            view.text_input_layout_password.isErrorEnabled = true
            view.text_input_layout_password.error = getString(R.string.wrong_password)
        }

        return view
    }

    private fun firebaseExceptionHandling(e: FirebaseException, showToast: Boolean = true, `throw`: Boolean = false) {
        firebaseExceptionHandler.exceptionHandling(e, showToast, `throw`)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CODE_GOOGLE_SIGN_IN -> {
                try {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    val account = task.getResult(ApiException::class.java)
                    firebaseAuthWithGoogle(account)
                } catch (e: ApiException) {
                    showToast(requireContext(), getString(R.string.failed_to_sign_in_with_google))
                    e.printStackTrace()
                }
            }
        }
    }

    private fun signInWithEmail() {
        if (edit_text_email.text?.isBlank() == true) {
            showToast(requireContext(), getString(R.string.please_enter_your_email))
            text_input_layout_email.isErrorEnabled = true
            text_input_layout_email.error = getString(R.string.please_enter_your_email)
            return
        }

        if (edit_text_password.text?.isBlank() == true) {
            showToast(requireContext(), getString(R.string.please_enter_your_password))
            text_input_layout_password.isErrorEnabled = true
            text_input_layout_password.error = getString(R.string.please_enter_your_password)
            return
        }

        val email = edit_text_email.text.toString()
        val password = edit_text_password.text.toString()

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful)
                    Timber.d("Email sign in successful")
                else
                    firebaseExceptionHandling(task.exception!! as FirebaseException)
            }
    }

    private fun signInWithGoogle() {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient =
            GoogleSignIn.getClient(requireContext(), googleSignInOptions)
        val signInIntent = googleSignInClient?.signInIntent

        startActivityForResult(signInIntent, REQUEST_CODE_GOOGLE_SIGN_IN)
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful)
                Timber.d("Google sign in successful")
            else
                firebaseExceptionHandling(task.exception!! as FirebaseException)
        }
    }

    private fun signInWithFacebook() {
        LoginManager.getInstance().loginBehavior = LoginBehavior.NATIVE_WITH_FALLBACK
        LoginManager.getInstance()
            .logInWithReadPermissions(requireActivity(), listOf("public_profile", "email"))
        LoginManager.getInstance().registerCallback((activity as MainActivity).callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {
                    firebaseAuthWithFacebook(result)
                }

                override fun onCancel() {
                    Timber.e("Facebook sign in canceled")
                }

                override fun onError(error: FacebookException?) {
                    showToast(requireContext(), getString(R.string.failed_to_sign_in_with_facebook))
                    error?.printStackTrace()
                }
            })
    }

    private fun firebaseAuthWithFacebook(result: LoginResult?) {
        val credential = FacebookAuthProvider.getCredential(result?.accessToken?.token!!)
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful)
                Timber.d("Facebook sign in successful")
            else
                firebaseExceptionHandling(task.exception!! as FirebaseException)
        }
    }

    private fun signInWithTwitter() {
        val provider = OAuthProvider.newBuilder("twitter.com")
        val pendingResultTask = (requireActivity() as MainActivity).firebaseAuth.pendingAuthResult
        if (pendingResultTask != null) {
            pendingResultTask
                .addOnSuccessListener(
                    OnSuccessListener { authResult ->
                        // IdP Data: authResult.getAdditionalUserInfo().getProfile()
                        // OAuth Access Token: authResult.getCredential().getAccessToken()
                        // OAuth Secret: authResult.getCredential().getSecret()
                        if (authResult != null)
                            firebaseAuthWithTwitter(authResult)
                        else {
                            showToast(requireContext(), getString(R.string.failed_to_sign_in_with_twitter))
                            Timber.e("authResult is null")
                        }
                    })
                .addOnFailureListener{
                    showToast(requireContext(), getString(R.string.failed_to_sign_in_with_twitter))
                    it.printStackTrace()
                }
        } else {
            (requireActivity() as MainActivity).firebaseAuth
                .startActivityForSignInWithProvider(requireActivity(), provider.build())
                .addOnSuccessListener{ authResult ->
                    firebaseAuthWithTwitter(authResult)
                }
                .addOnFailureListener {
                    showToast(requireContext(), getString(R.string.failed_to_sign_in_with_twitter))
                    it.printStackTrace()
                }
        }
    }

    private fun firebaseAuthWithTwitter(result: AuthResult) {
        val credential = result.credential
        if (credential != null) {
            FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener { task ->
                if (task.isSuccessful)
                    Timber.d("Twitter sign in successful")
                else {
                    firebaseExceptionHandling(task.exception!! as FirebaseException)
                }
            }
        } else {
            showToast(requireContext(), getString(R.string.failed_to_sign_in_with_twitter))
            Timber.e("credential is null")
        }
    }

    private fun startSignUpFragment() {
        (requireActivity() as MainActivity).startFragment(
            SignUpFragment(),
            R.id.frame_layout_activity_main, MainActivity.TAG_SIGN_UP_FRAGMENT
        )
    }

    companion object {
        @JvmStatic
        fun newInstance() = SignInFragment()
    }
}