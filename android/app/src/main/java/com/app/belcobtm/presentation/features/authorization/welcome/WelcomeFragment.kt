package com.app.belcobtm.presentation.features.authorization.welcome

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.belcobtm.R
import com.app.belcobtm.domain.authorization.AuthorizationStatus
import com.app.belcobtm.presentation.core.Const.TERMS_URL
import com.app.belcobtm.presentation.core.helper.SimpleClickableSpan
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.features.authorization.wallet.create.CreateWalletActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_welcome.*
import kotlinx.android.synthetic.main.view_support_dialog.*
import org.koin.android.viewmodel.ext.android.viewModel

class WelcomeFragment : BaseFragment() {
    private val viewModel: WelcomeViewModel by viewModel()
    override val isToolbarEnabled: Boolean = false
    override val resourceLayout: Int = R.layout.fragment_welcome
    override val backPressedListener: View.OnClickListener = View.OnClickListener { requireActivity().finish() }

    override fun onStart() {
        super.onStart()
        viewModel.clearAppData()
    }

    override fun initViews() {
        val authorizationStatus = requireArguments().getInt(TAG_AUTHORIZATION_STATUS, -1)
        requireArguments().remove(TAG_AUTHORIZATION_STATUS)
        when (authorizationStatus) {
            AuthorizationStatus.SEED_PHRASE_ENTER.ordinal -> navigate(R.id.to_recover_seed_fragment)
            else -> {
                initTncView()
                pagerView.apply {
                    adapter = WelcomePagerAdapter()
                    adapter?.registerAdapterDataObserver(pagerIndicatorView.adapterDataObserver)
                    (getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
                }
                pagerIndicatorView.setViewPager(pagerView)
            }
        }
    }

    override fun initListeners() {
        contactSupportButtonView.setOnClickListener { showSupportDialog() }
        createNewWalletButtonView.setOnClickListener { openCreateWalletScreen() }
        recoverWalletButtonView.setOnClickListener { openRecoverWallerScreen() }
    }

    private fun initTncView() {
        val linkClickableSpan = SimpleClickableSpan(
            onClick = { CustomTabsIntent.Builder().build().launchUrl(requireContext(), Uri.parse(TERMS_URL)) },
            updateDrawState = { it.isUnderlineText = false }
        )
        val defaultTextClickableSpan = SimpleClickableSpan(
            onClick = { tncCheckBoxView.isChecked = !tncCheckBoxView.isChecked },
            updateDrawState = {
                it.isUnderlineText = false
                it.color = ContextCompat.getColor(requireContext(), R.color.colorText)
            }
        )
        val linkText = getString(R.string.welcome_screen_terms_and_conditions)
        val fullText = SpannableString(getString(R.string.welcome_screen_accept_terms_and_conditions))
        val startIndex = fullText.indexOf(linkText, 0, true)
        fullText.setSpan(
            linkClickableSpan,
            startIndex,
            fullText.length,
            Spanned.SPAN_INCLUSIVE_INCLUSIVE
        )
        fullText.setSpan(
            defaultTextClickableSpan,
            0,
            startIndex,
            Spanned.SPAN_INCLUSIVE_INCLUSIVE
        )
        tncTextView.text = TextUtils.expandTemplate(fullText, linkText)
        tncTextView.movementMethod = LinkMovementMethod.getInstance()
        tncTextView.highlightColor = Color.TRANSPARENT
    }

    private fun openCreateWalletScreen(): Unit = if (tncCheckBoxView.isChecked) {
        startActivity(Intent(requireContext(), CreateWalletActivity::class.java))
    } else {
        showSnackBar(R.string.welcome_screen_please_accept_tnc)
    }

    private fun openRecoverWallerScreen(): Unit = if (tncCheckBoxView.isChecked) {
        navigate(R.id.to_recover_wallet_fragment)
    } else {
        showSnackBar(R.string.welcome_screen_please_accept_tnc)
    }

    private fun showSupportDialog() {
        val dialog = MaterialAlertDialogBuilder(contactSupportButtonView.context)
            .setView(R.layout.view_support_dialog)
            .show()
        val supportEmail = getString(R.string.welcome_screen_support_email)
        val supportPhone = getString(R.string.welcome_screen_support_phone)
        dialog.callButtonView.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$supportPhone")
            startActivity(intent)
            dialog.cancel()
        }
        dialog.emailButtonView.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:$supportEmail")
            if (intent.resolveActivity(contactSupportButtonView.context.packageManager) != null) {
                startActivity(intent)
                dialog.cancel()
            }
        }
        dialog.cancelButtonView.setOnClickListener { dialog.cancel() }
        dialog.setCanceledOnTouchOutside(false)
    }

    companion object {
        const val TAG_AUTHORIZATION_STATUS = "tag_authorization_status"
    }
}
