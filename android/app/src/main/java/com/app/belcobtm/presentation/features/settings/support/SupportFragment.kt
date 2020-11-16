package com.app.belcobtm.presentation.features.settings.support

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.view.View
import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import kotlinx.android.synthetic.main.fragment_support.*
import java.util.*

class SupportFragment : BaseFragment() {

    override val resourceLayout: Int = R.layout.fragment_support
    override val isHomeButtonEnabled: Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolbarTitle(R.string.support_title)
        setupPhoneNumber()
        setClickListeners()
    }

    private fun setupPhoneNumber() {
        val supportPhone = getString(R.string.support_phone)
        val formattedSupportPhone = PhoneNumberUtils.formatNumber(
            supportPhone,
            Locale.US.country
        )
        phoneItem.setValue(formattedSupportPhone)
    }

    private fun setClickListeners() {
        phoneItem.setOnClickListener {
            val phoneNumber = getString(R.string.support_phone)
            val callURI = Uri.fromParts("tel", phoneNumber, null)
            val dialIntent = Intent(Intent.ACTION_DIAL, callURI)
            startIntentSafe(dialIntent)
        }
        emailItem.setOnClickListener {
            val email = getString(R.string.support_email)
            val emailURI = Uri.fromParts("mailto", email, null)
            val mainIntent = Intent(Intent.ACTION_SENDTO, emailURI)
            startIntentSafe(mainIntent)
        }
        telegramItem.setOnClickListener {
            val tgURI = Uri.parse("http://www.telegram.me/belco_support")
            val telegramIntent = Intent(Intent.ACTION_VIEW, tgURI)
            startIntentSafe(telegramIntent)
        }
        whatsAppItem.setOnClickListener {
            val whatsAppUri = Uri.parse("https://chat.whatsapp.com/HLM8HlzE5VjDjhEiZJpKJr")
            val whatsAppIntent = Intent(Intent.ACTION_VIEW, whatsAppUri)
            startIntentSafe(whatsAppIntent)
        }
    }

    private fun startIntentSafe(intent: Intent) {
        val chooserMessage = getString(R.string.support_chooser_message)
        val chooserActivity = Intent.createChooser(intent, chooserMessage)
        startActivity(chooserActivity)
    }
}
