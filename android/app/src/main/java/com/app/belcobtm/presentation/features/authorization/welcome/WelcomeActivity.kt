package com.app.belcobtm.presentation.features.authorization.welcome

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.Const.TERMS_URL
import com.app.belcobtm.presentation.features.authorization.wallet.create.CreateWalletActivity
import com.app.belcobtm.presentation.features.authorization.wallet.recover.RecoverWalletActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_welcome.*
import kotlinx.android.synthetic.main.view_support_dialog.*
import org.koin.android.viewmodel.ext.android.viewModel

class WelcomeActivity : AppCompatActivity() {
    private val viewModel: WelcomeViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        initViews()
        initListeners()
    }

    override fun onStart() {
        super.onStart()
        viewModel.clearAppData()
    }

    private fun initViews() {
        val fullText = SpannableString(getString(R.string.welcome_screen_accept_terms_and_conditions))
        val linkText = getString(R.string.welcome_screen_terms_and_conditions)
        val startLinkPosition = fullText.indexOf(linkText)
        val endLinkPosition = startLinkPosition + linkText.length
        val colorSpan =
            ForegroundColorSpan(ContextCompat.getColor(tncCheckBoxView.context, R.color.colorAccent))
        fullText.setSpan(colorSpan, startLinkPosition, endLinkPosition, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        tncCheckBoxView.text = fullText
        pagerView.apply {
            adapter = WelcomePagerAdapter()
            adapter?.registerAdapterDataObserver(pagerIndicatorView.adapterDataObserver)
            (getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }
        pagerIndicatorView.setViewPager(pagerView)
    }

    private fun initListeners() {
        contactSupportButtonView.setOnClickListener { showSupportDialog() }
        createNewWalletButtonView.setOnClickListener { openCreateWalletScreen() }
        recoverWalletButtonView.setOnClickListener { openRecoverWallerScreen() }
        tncCheckBoxView.setOnTouchListener { view, event ->
            when {
                event.action == MotionEvent.ACTION_UP && event.x <= view.height -> {
                    //empty
                    false
                }
                event.action == MotionEvent.ACTION_UP && event.x >= view.height -> {
                    openTncScreen()
                    true
                }
                else -> false
            }
        }
    }

    private fun openTncScreen(): Unit = CustomTabsIntent.Builder().build().launchUrl(this, Uri.parse(TERMS_URL))

    private fun openCreateWalletScreen(): Unit = if (tncCheckBoxView.isChecked)
        startActivity(Intent(this, CreateWalletActivity::class.java))
    else {
        showTermsErrorAlert()
    }

    private fun openRecoverWallerScreen(): Unit = if (tncCheckBoxView.isChecked)
        startActivity(Intent(this, RecoverWalletActivity::class.java))
    else {
        showTermsErrorAlert()
    }

    private fun showTermsErrorAlert(): Unit = Snackbar.make(
        container,
        R.string.welcome_screen_please_accept_tnc,
        Snackbar.LENGTH_SHORT
    ).also { it.view.setBackgroundColor(getColor(R.color.colorErrorSnackBar)) }.show()

    private fun showSupportDialog() {
        val dialog = MaterialAlertDialogBuilder(this)
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
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
                dialog.cancel()
            }
        }
        dialog.cancelButtonView.setOnClickListener { dialog.cancel() }
    }
}
