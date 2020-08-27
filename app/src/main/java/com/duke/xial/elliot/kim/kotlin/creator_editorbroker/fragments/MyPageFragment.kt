package com.duke.xial.elliot.kim.kotlin.creator_editorbroker.fragments

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.R
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.activities.MainActivity
import com.duke.xial.elliot.kim.kotlin.creator_editorbroker.constants.VERTICAL
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.android.synthetic.main.fragment_my_page.view.*

class MyPageFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_page, container, false)

        initializeToolbar(view.toolbar)

        view.text_view_public_name.text = MainActivity.currentUserInformation?.publicName

        view.text_view_write_pr.setOnClickListener {
            (requireActivity() as MainActivity).startFragment(WritePrFragment(),
                R.id.frame_layout_activity_main,
                MainActivity.TAG_WRITE_PR_FRAGMENT,
                VERTICAL)
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
        inflater.inflate(R.menu.menu_my_page, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> requireActivity().onBackPressed()
            R.id.item_settings -> {  }
            R.id.item_sign_out -> signOut()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun signOut() {
        (requireActivity() as MainActivity).firebaseAuth.signOut()
        googleSignInClientSignOut()
        LoginManager.getInstance().logOut()
    }

    private fun googleSignInClientSignOut() {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(requireContext(), googleSignInOptions)
        googleSignInClient.signOut()
    }

    private fun clearUi() {

    }

    companion object {
        @JvmStatic
        fun newInstance() = MyPageFragment()
    }
}