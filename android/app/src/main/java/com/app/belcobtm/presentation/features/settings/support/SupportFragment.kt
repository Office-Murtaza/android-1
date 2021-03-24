package com.app.belcobtm.presentation.features.settings.support

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.belcobtm.R
import com.app.belcobtm.databinding.FragmentSupportBinding
import com.app.belcobtm.presentation.core.formatter.PhoneNumberFormatter
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import org.koin.android.ext.android.inject

class SupportFragment : BaseFragment<FragmentSupportBinding>() {

    override val isHomeButtonEnabled: Boolean = true

    private val phoneNumberFormatter: PhoneNumberFormatter by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolbarTitle(R.string.support_title)
        setupPhoneNumber()
        setClickListeners()
    }

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentSupportBinding =
        FragmentSupportBinding.inflate(inflater, container, false)

    private fun setupPhoneNumber() {
        val supportPhone = getString(R.string.support_phone)
        binding.phoneItem.setValue(phoneNumberFormatter.format(supportPhone))
    }

    private fun setClickListeners() {
        binding.phoneItem.setOnClickListener {
            val phoneNumber = getString(R.string.support_phone)
            val callURI = Uri.fromParts("tel", phoneNumber, null)
            val dialIntent = Intent(Intent.ACTION_DIAL, callURI)
            startIntentSafe(dialIntent)
        }
        binding.emailItem.setOnClickListener {
            val email = getString(R.string.support_email)
            val emailURI = Uri.fromParts("mailto", email, null)
            val mainIntent = Intent(Intent.ACTION_SENDTO, emailURI)
            startIntentSafe(mainIntent)
        }
        binding.telegramItem.setOnClickListener {
            val tgURI = Uri.parse("http://www.telegram.me/belco_support")
            val telegramIntent = Intent(Intent.ACTION_VIEW, tgURI)
            startIntentSafe(telegramIntent)
        }
        binding.whatsAppItem.setOnClickListener {
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
