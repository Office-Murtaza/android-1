package com.app.belcobtm.presentation.features.settings

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.tools.IntentActions
import com.app.belcobtm.presentation.core.Const
import com.app.belcobtm.presentation.features.settings.SettingsFragment.Companion.SETTINGS_ABOUT
import com.app.belcobtm.presentation.features.settings.SettingsFragment.Companion.SETTINGS_MAIN
import com.app.belcobtm.presentation.features.settings.SettingsFragment.Companion.SETTINGS_SECURITY

class SettingsViewModel(val appContext: Context, val intentActions: IntentActions) : ViewModel() {

    val stateData = MutableLiveData<SettingsState>(
        SettingsState(
            versionName = appContext.packageManager.getPackageInfo(
                appContext.packageName,
                0
            ).versionName
        )
    )

    fun onSectionClick(section: SettingsSections) {
        when (section) {
            SettingsSections.SECURITY -> {
                stateData.value = stateData.value?.copy(
                    viewFlipperValue = SETTINGS_SECURITY,
                    showBackButton = true
                )
            }
            SettingsSections.KYC -> {

            }
            SettingsSections.ABOUT -> {
                stateData.value = stateData.value?.copy(
                    viewFlipperValue = SETTINGS_ABOUT,
                    showBackButton = true
                )
            }
        }
    }

    fun onBackPress() {
        if (stateData.value?.viewFlipperValue != SETTINGS_MAIN) {
            stateData.value = stateData.value?.copy(
                viewFlipperValue = SETTINGS_MAIN,
                showBackButton = false
            )
        }
    }

    fun onAboutItemClick(aboutItem: AboutItems) {
        when (aboutItem) {
            AboutItems.TERMS -> {
                intentActions.openViewActivity(Const.TERMS_URL)
            }
            AboutItems.SUPPORT -> {
                intentActions.openViewActivity(Const.SUPPORT_URL)
            }
        }
    }

    fun onSecurityItemClick(securityItem: SecurityItems) {
        when (securityItem) {
            SecurityItems.PHONE -> TODO()
            SecurityItems.PASS -> TODO()
            SecurityItems.PIN -> TODO()
            SecurityItems.SEED -> TODO()
            SecurityItems.UNLINK -> TODO()
        }
    }
}

enum class SettingsSections {
    SECURITY,
    KYC,
    ABOUT
}

enum class AboutItems {
    TERMS,
    SUPPORT
}

enum class SecurityItems {
    PHONE,
    PASS,
    PIN,
    SEED,
    UNLINK
}

data class SettingsState(
    val viewFlipperValue: Int = 0,
    val versionName: String,
    val showBackButton: Boolean = false
)
