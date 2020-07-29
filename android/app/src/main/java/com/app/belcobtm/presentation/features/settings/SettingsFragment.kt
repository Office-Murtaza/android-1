package com.app.belcobtm.presentation.features.settings

import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.layout_settings_about.*
import kotlinx.android.synthetic.main.layout_settings_main.*
import kotlinx.android.synthetic.main.layout_settings_security.*
import org.koin.android.viewmodel.ext.android.viewModel

class SettingsFragment : BaseFragment() {
    val viewModel by viewModel<SettingsViewModel>()
    private var appliedState: SettingsState? = null
    override val resourceLayout = R.layout.fragment_settings
    override val isMenuEnabled = true

    override val backPressedListener = View.OnClickListener {
        viewModel.onBackPress()
    }

    companion object {
        const val SETTINGS_MAIN = 0
        const val SETTINGS_SECURITY = 1
        const val SETTINGS_ABOUT = 2
    }

    override fun initViews() {
        setToolbarTitle(R.string.settings)
    }

    override fun initListeners() {
        //sections listener
        securityItem.setOnClickListener { onSectionClick(SettingsSections.SECURITY) }
        kycItem.setOnClickListener { onSectionClick(SettingsSections.KYC) }
        aboutItem.setOnClickListener { onSectionClick(SettingsSections.ABOUT) }

        //about listeners
        termsItem.setOnClickListener { onAboutItemClick(AboutItems.TERMS) }
        supportItem.setOnClickListener { onAboutItemClick(AboutItems.SUPPORT) }

        //security listeners
        updatePhoneItem.setOnClickListener { onSecurityItemClick(SecurityItems.PHONE) }
        updatePassItem.setOnClickListener { onSecurityItemClick(SecurityItems.PASS) }
        updatePinItem.setOnClickListener { onSecurityItemClick(SecurityItems.PIN) }
        seedPhraseItem.setOnClickListener { onSecurityItemClick(SecurityItems.SEED) }
        unlinkItem.setOnClickListener { onSecurityItemClick(SecurityItems.UNLINK) }
    }

    override fun initObservers() {
        viewModel.stateData.observe(this, Observer { state ->
            state.viewFlipperValue.doIfChanged(appliedState?.viewFlipperValue, {
                flipper.displayedChild = it
            })
            state.versionName.doIfChanged(appliedState?.versionName, {
                versionItem.setValue(it)
            })
            state.showBackButton.doIfChanged(appliedState?.showBackButton, {
                isBackButtonEnabled = it
                showBackButton(it)
            })
            appliedState = state
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            viewModel.onBackPress()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun onSectionClick(section: SettingsSections) {
        viewModel.onSectionClick(section)
    }

    private fun onAboutItemClick(aboutItem: AboutItems) {
        viewModel.onAboutItemClick(aboutItem)
    }

    private fun onSecurityItemClick(securityItem: SecurityItems) {
        viewModel.onSecurityItemClick(securityItem)
    }
}

fun <T> T.doIfChanged(old: T?, action: (T) -> Unit) {
    if (this != old) {
        action(this)
    }
}