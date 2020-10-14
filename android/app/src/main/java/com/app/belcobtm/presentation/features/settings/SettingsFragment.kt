package com.app.belcobtm.presentation.features.settings

import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import kotlinx.android.synthetic.main.layout_settings_main.*
import org.koin.android.viewmodel.ext.android.viewModel

class SettingsFragment : BaseFragment() {

    private val viewModel by viewModel<SettingsViewModel>()
    private val settingsArgs: SettingsFragmentArgs by navArgs()

    override val resourceLayout = R.layout.layout_settings_main
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
        securityItem.setOnClickListener { onSectionClick(SettingsSections.SECURITY) }
        kycItem.setOnClickListener { onSectionClick(SettingsSections.KYC) }
        aboutItem.setOnClickListener { onSectionClick(SettingsSections.ABOUT) }
    }

    override fun initObservers() {
        viewModel.actionData.observe(this, Observer { action ->
            when (action) {
                is SettingsAction.NavigateAction -> navigate(action.navDirections)
            }
        })
    }

    private fun onSectionClick(section: SettingsSections) {
        viewModel.onSectionClick(section)
    }
}
