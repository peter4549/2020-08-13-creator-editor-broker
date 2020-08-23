package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.activities.MainActivity
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.constants.REQUEST_CODE_GOOGLE_SIGN_IN
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
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.fragment_sign_in.*
import kotlinx.android.synthetic.main.fragment_sign_in.view.*

class SignInFragment : Fragment() {

    private val buttonClickListener = View.OnClickListener { view ->
        when(view.id) {
            R.id.button_sign_in_with_email -> signInWithEmail()
            R.id.button_sign_in_with_google -> signInWithGoogle()
            R.id.button_sign_in_with_facebook -> signInWithFacebook()
            R.id.button_sign_up -> startSignUpFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sign_in, container, false)
        initializeToolbar(view.toolbar)
        view.button_sign_in_with_email.setOnClickListener(buttonClickListener)
        view.button_sign_in_with_google.setOnClickListener(buttonClickListener)
        view.button_sign_in_with_facebook.setOnClickListener(buttonClickListener)
        view.button_sign_up.setOnClickListener(buttonClickListener)

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

    private fun signInWithEmail() {
        if (edit_text_email.text.isBlank()) {
            showToast(requireContext(), getString(R.string.please_enter_email))
            return
        }

        if (edit_text_password.text.isBlank()) {
            showToast(requireContext(), getString(R.string.please_enter_password))
            return
        }

        val email = edit_text_email.text.toString()
        val password = edit_text_password.text.toString()

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful)
                    println("$TAG: sign in with email")
                else
                    (requireActivity() as MainActivity).errorHandler
                        .errorHandling(task.exception!!, getString(R.string.failed_to_sign_in_with_email))
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
            if (task.isSuccessful) {
                println("$TAG: Google sign in successful")
            }
            else
                (requireActivity() as MainActivity).errorHandler
                    .errorHandling(task.exception!!)
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

    private fun startSignUpFragment() {
        (requireActivity() as MainActivity).startFragment(SignUpFragment(),
            R.id.constraint_layout_activity_main, MainActivity.TAG_SIGN_UP_FRAGMENT)
    }

    companion object {
        private const val TAG = "SignInFragment"
        @JvmStatic
        fun newInstance() = SignInFragment()
    }
}