package com.belcobtm.presentation.features.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.belcobtm.R
import com.belcobtm.databinding.FragmentSettingsBinding
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.features.MainFragment
import com.belcobtm.presentation.features.MainFragment.Companion.INNER_DESTINATION_ID
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : BaseFragment<FragmentSettingsBinding>() {

    private val viewModel by viewModel<SettingsViewModel>()
    private val settingsArgs: SettingsFragmentArgs by navArgs()

    override var isMenuEnabled = true

    companion object {
        const val SETTINGS_MAIN = 0
        const val SETTINGS_SECURITY = 1
        const val SETTINGS_ABOUT = 2
    }

    override fun FragmentSettingsBinding.initViews() {
        setToolbarTitle(R.string.settings)
        viewModel.processArgs(settingsArgs)
    }

    override fun FragmentSettingsBinding.initListeners() {
        //sections listener
        walletsItem.setOnClickListener { onSectionClick(SettingsSections.WALLETS) }
        securityItem.setOnClickListener { onSectionClick(SettingsSections.SECURITY) }
        kycItem.setOnClickListener { onSectionClick(SettingsSections.KYC) }
        notificationsItem.setOnClickListener { onSectionClick(SettingsSections.NOTIFICATIONS) }
        supportItem.setOnClickListener { onSectionClick(SettingsSections.SUPPORT) }
        aboutItem.setOnClickListener { onSectionClick(SettingsSections.ABOUT) }
        referralsItems.setOnClickListener { onSectionClick(SettingsSections.REFERRALS) }
    }

    override fun FragmentSettingsBinding.initObservers() {
        viewModel.actionData.observe(viewLifecycleOwner) { action ->
            when (action) {
                is SettingsAction.NavigateAction -> navigate(action.navDirections)
                SettingsAction.NotificationOptions -> startNotificationsSettings()
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

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSettingsBinding =
        FragmentSettingsBinding.inflate(inflater, container, false)
}
