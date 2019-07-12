package com.app.belcobtm.ui.auth.welcome

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
import com.app.belcobtm.model.WelcomePagerItem
import com.app.belcobtm.ui.auth.create_wallet.CreateWalletActivity
import com.app.belcobtm.util.Const.TERMS_URL
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_welcome.*
import org.jetbrains.anko.design.snackbar


class WelcomeActivity : AppCompatActivity() {

    private val pagerItems: ArrayList<WelcomePagerItem> = ArrayList()

    init {
        pagerItems.add(WelcomePagerItem(R.drawable.ic_welcome_slide1, R.string.welcome_slide_1))
        pagerItems.add(WelcomePagerItem(R.drawable.ic_welcome_slide2, R.string.welcome_slide_2))
        pagerItems.add(WelcomePagerItem(R.drawable.ic_welcome_slide3, R.string.welcome_slide_3))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val adapter = WelcomePagerAdapter(pagerItems)
        view_pager.adapter = adapter
        view_pager_indicator.setViewPager(view_pager)
        adapter.registerAdapterDataObserver(view_pager_indicator.adapterDataObserver)

        terms.setOnClickListener { openTerms() }
        contact_support.setOnClickListener { openSupportDialog() }

        create_a_new_wallet.setOnClickListener { onClickCreateNewWallet() }
        login_to_my_wallet.setOnClickListener { onClickLoginMyWallet() }
        recover_my_wallet.setOnClickListener { showInProgress() }//todo
    }

    private fun openSupportDialog() {
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

    private fun openTerms() {
        CustomTabsIntent.Builder()
            .build()
            .launchUrl(this, Uri.parse(TERMS_URL))
    }

    private fun copyToClipboard(toastText: String, copiedText: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(toastText, copiedText)
        clipboard.primaryClip = clip
    }


    private fun onClickCreateNewWallet() {
        if (accept_terms.isChecked)
            startActivity(Intent(this, CreateWalletActivity::class.java))
        else {
            showAcceptTermsError()
        }
    }

    private fun onClickLoginMyWallet() {
        if (accept_terms.isChecked)
            showInProgress()//todo
        else {
            showAcceptTermsError()
        }
    }

    //todo remove when not needed
    private fun showInProgress() {
        container.snackbar("In progress...")
    }

    private fun showAcceptTermsError() {
        val snackbar = Snackbar.make(container, R.string.please_accept_terms, Snackbar.LENGTH_SHORT)
        snackbar.view.setBackgroundColor(getColor(R.color.error_color_material_light))
        snackbar.show()
    }


}
