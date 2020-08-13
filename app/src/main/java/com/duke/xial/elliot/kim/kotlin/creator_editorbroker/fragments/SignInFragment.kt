package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.activities.MainActivity
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.constants.REQUEST_CODE_GOOGLE_SIGN_IN
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.error_handler.ErrorHandler
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.utilities.showToast
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginBehavior
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.fragment_sign_in.view.*

class SignInFragment : Fragment() {

    private val buttonClickListener = View.OnClickListener { view ->
        when(view.id) {
            R.id.button_sign_in_with_email -> {  }
            R.id.button_sign_in_with_google -> signInWithGoogle()
            R.id.button_sign_in_with_facebook -> signInWithFacebook()
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

        return view
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
                    (requireActivity() as MainActivity).errorHandler
                        .errorHandling(e, getString(R.string.failed_to_sign_in_with_google))
                }
            }
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
                println("$TAG: Google sign in successful")
            else
                (requireActivity() as MainActivity).errorHandler
                    .errorHandling(task.exception ?:
                Exception("failed to Google sign in, task.exception is null"),
                    getString(R.string.failed_to_sign_in_with_google))
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
                println("$TAG: Facebook sign in canceled")
            }

            override fun onError(error: FacebookException?) {
                (requireActivity() as MainActivity).errorHandler
                    .errorHandling(error ?: FacebookException("failed to sign in with Facebook"),
                    getString(R.string.failed_to_sign_in_with_facebook))
            }
        })
    }

    private fun firebaseAuthWithFacebook(result: LoginResult?) {
        val credential = FacebookAuthProvider.getCredential(result?.accessToken?.token!!)
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {
                task ->
            if (task.isSuccessful)
                println("$TAG: Facebook sign in successful")
            else {
                //showToast(requireContext(), getString(R.string.authentication_failure_message))
                println("$TAG: ${task.exception}")
            }
        }
    }

    companion object {
        private const val TAG = "SignInFragment"
        @JvmStatic
        fun newInstance() = SignInFragment()
    }
}