package com.app.belcobtm.presentation.features.settings

import android.content.Intent
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import kotlinx.android.synthetic.main.fragment_settings.*
import org.koin.android.viewmodel.ext.android.viewModel

class SettingsFragment : BaseFragment() {

    private val viewModel by viewModel<SettingsViewModel>()
    private val settingsArgs: SettingsFragmentArgs by navArgs()

    override val resourceLayout = R.layout.fragment_settings
    override var isMenuEnabled = true

    companion object {
        const val SETTINGS_MAIN = 0
        const val SETTINGS_SECURITY = 1
        const val SETTINGS_ABOUT = 2
    }

    override fun initViews() {
        setToolbarTitle(R.string.settings)
        viewModel.processArgs(settingsArgs)
    }

    override fun initListeners() {
        //sections listener
        walletsItem.setOnClickListener { onSectionClick(SettingsSections.WALLETS) }
        securityItem.setOnClickListener { onSectionClick(SettingsSections.SECURITY) }
        kycItem.setOnClickListener { onSectionClick(SettingsSections.KYC) }
        notificationsItem.setOnClickListener { onSectionClick(SettingsSections.NOTIFICATIONS) }
        supportItem.setOnClickListener { onSectionClick(SettingsSections.SUPPORT) }
        aboutItem.setOnClickListener { onSectionClick(SettingsSections.ABOUT) }
    }

    override fun initObservers() {
        viewModel.actionData.observe(this, Observer { action ->
            when (action) {
                is SettingsAction.NavigateAction -> navigate(action.navDirections)
                SettingsAction.NotificationOptions -> startNotificationsSettings()
            }
        })
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
}
