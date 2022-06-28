package com.belcobtm.presentation.features.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.navArgs
import com.belcobtm.R
import com.belcobtm.databinding.FragmentSettingsBinding
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.features.sms.code.SmsCodeFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import zendesk.android.Zendesk

class SettingsFragment : BaseFragment<FragmentSettingsBinding>() {

    private val viewModel by viewModel<SettingsViewModel>()
    private val settingsArgs: SettingsFragmentArgs by navArgs()

    override var isMenuEnabled = true

    companion object {

        const val SETTINGS_MAIN = 0
        const val SETTINGS_SECURITY = 1
        const val SETTINGS_ABOUT = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(SmsCodeFragment.REQUEST_KEY) { requestKey, bundle ->
            if (bundle.getBoolean(SmsCodeFragment.BUNDLE_KEY_PHONE_UPDATE_VERIFICATION)) {
                viewModel.updatePhone()
            }
        }
    }

    override fun FragmentSettingsBinding.initViews() {
        setToolbarTitle(R.string.settings)
        viewModel.processArgs(settingsArgs)
        setUnreadSupportMessages()
    }

    private fun setUnreadSupportMessages() {
        binding.supportItem.setValue(
            Zendesk.instance.messaging
                .getUnreadMessageCount()
                .takeIf { it > 0 }
                ?.toString() ?: ""
        )
    }

    override fun FragmentSettingsBinding.initListeners() {
        walletsItem.setOnClickListener { onSectionClick(SettingsSections.WALLETS) }
        securityItem.setOnClickListener { onSectionClick(SettingsSections.SECURITY) }
        kycItem.setOnClickListener { onSectionClick(SettingsSections.KYC) }
        notificationsItem.setOnClickListener { onSectionClick(SettingsSections.NOTIFICATIONS) }
        supportItem.setOnClickListener { onSectionClick(SettingsSections.SUPPORT) }
        aboutItem.setOnClickListener { onSectionClick(SettingsSections.ABOUT) }
        referralsItems.setOnClickListener { onSectionClick(SettingsSections.REFERRALS) }
    }

    override fun FragmentSettingsBinding.initObservers() {
        viewModel.apply {

            actionData.observe(viewLifecycleOwner) { action ->
                when (action) {
                    is SettingsAction.NavigateAction -> navigate(action.navDirections)
                    SettingsAction.NotificationOptions -> startNotificationsSettings()
                    SettingsAction.SupportChat -> openSupportChat()
                    SettingsAction.PhoneSuccessfullyUpdated -> showPhoneUpdatedSuccess()
                    SettingsAction.PhoneUpdateFailed -> showPhoneUpdatedFail()
                }
            }
        }
    }

    private fun onSectionClick(section: SettingsSections) {
        viewModel.onSectionClick(section)
    }

    private fun startNotificationsSettings() {
        val intent = Intent()
        intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
        intent.putExtra("app_package", activity?.packageName)
        intent.putExtra("app_uid", activity?.applicationInfo?.uid)
        intent.putExtra("android.provider.extra.APP_PACKAGE", activity?.packageName)
        startActivity(intent)
    }

    private fun openSupportChat() {
        Zendesk.instance.messaging.showMessaging(requireActivity())
    }

    private fun showPhoneUpdatedSuccess() {
        showToast(getString(R.string.update_phone_success))
    }

    private fun showPhoneUpdatedFail() {
        showToast(getString(R.string.update_phone_fail))
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSettingsBinding =
        FragmentSettingsBinding.inflate(inflater, container, false)

}
