package com.app.belcobtm.presentation.features.authorization.welcome

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.browser.customtabs.CustomTabsIntent
import com.app.belcobtm.R
import com.app.belcobtm.core.Const.TERMS_URL
import com.app.belcobtm.ui.auth.create_wallet.CreateWalletActivity
import com.app.belcobtm.ui.auth.recover_wallet.RecoverWalletActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_welcome.*
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
        pagerIndicatorView.setViewPager(pagerView)
        pagerView.adapter = WelcomePagerAdapter()
        pagerView.adapter?.registerAdapterDataObserver(pagerIndicatorView.adapterDataObserver)
    }

    private fun initListeners() {
        termsLinkView.setOnClickListener { openTncScreen() }
        contactSupportButtonView.setOnClickListener { showSupportDialog() }
        createWalletButtonView.setOnClickListener { openCreateWalletScreen() }
        recoverWalletButtonView.setOnClickListener { openRecoverWallerScreen() }
    }

    private fun openTncScreen(): Unit = CustomTabsIntent.Builder().build().launchUrl(this, Uri.parse(TERMS_URL))

    private fun openCreateWalletScreen(): Unit = if (termsToggleView.isChecked)
        startActivity(Intent(this, CreateWalletActivity::class.java))
    else {
        showTermsErrorAlert()
    }

    private fun openRecoverWallerScreen(): Unit = if (termsToggleView.isChecked)
        startActivity(Intent(this, RecoverWalletActivity::class.java))
    else {
        showTermsErrorAlert()
    }

    private fun showTermsErrorAlert(): Unit = Snackbar.make(
        container,
        R.string.please_accept_terms,
        Snackbar.LENGTH_SHORT
    ).also { it.view.setBackgroundColor(getColor(R.color.colorErrorSnackBar)) }.show()

    private fun copyToClipboard(toastText: String, copiedText: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(toastText, copiedText)
        clipboard.setPrimaryClip(clip)
    }

    private fun showSupportDialog() {
        val dialog = AlertDialog
            .Builder(this)
            .setTitle(getString(R.string.support))
            .setPositiveButton(android.R.string.ok, null)
            .setView(R.layout.view_support_dialog)
            .create()

        val supportEmail = getString(R.string.support_email)
        val supportPhone = getString(R.string.support_phone)

        dialog.show()
        dialog.findViewById<AppCompatTextView>(R.id.copy_email)?.setOnClickListener {
            copyToClipboard(getString(R.string.support_email_clipboard), supportEmail)
            dialog.cancel()
            Snackbar.make(container, R.string.support_email_clipboard, Snackbar.LENGTH_LONG).show()
        }
        dialog.findViewById<AppCompatTextView>(R.id.copy_phone)?.setOnClickListener {
            copyToClipboard(getString(R.string.support_phone_clipboard), supportPhone)
            dialog.cancel()
            Snackbar.make(container, R.string.support_phone_clipboard, Snackbar.LENGTH_LONG).show()
        }
        dialog.findViewById<AppCompatImageView>(R.id.email_icon)?.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:$supportEmail")
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
                dialog.cancel()
            }
        }
        dialog.findViewById<AppCompatImageView>(R.id.phone_icon)?.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$supportPhone")
            startActivity(intent)
            dialog.cancel()
        }
    }
}
