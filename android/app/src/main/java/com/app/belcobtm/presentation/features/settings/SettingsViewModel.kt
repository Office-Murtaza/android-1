package com.app.belcobtm.presentation.features.settings

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import com.app.belcobtm.R
import com.app.belcobtm.domain.tools.IntentActions
import com.app.belcobtm.presentation.core.Const
import com.app.belcobtm.presentation.core.SingleLiveData
import com.app.belcobtm.presentation.features.authorization.create.seed.CreateSeedFragment
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

    val actionData = SingleLiveData<SettingsAction>()

    fun onSectionClick(section: SettingsSections) {
        when (section) {
            SettingsSections.SECURITY -> {
                stateData.value = stateData.value?.copy(
                    viewFlipperValue = SETTINGS_SECURITY,
                    showBackButton = true,
                    toolbarRes = getToolbarRes(SETTINGS_SECURITY)
                )
            }
            SettingsSections.KYC -> {
                actionData.value = SettingsAction.NavigateAction(SettingsFragmentDirections.settingsToVerificationInfoFragment())
            }
            SettingsSections.ABOUT -> {
                stateData.value = stateData.value?.copy(
                    viewFlipperValue = SETTINGS_ABOUT,
                    showBackButton = true,
                    toolbarRes = getToolbarRes(SETTINGS_ABOUT)
                )
            }
        }
    }

    fun onBackPress() {
        if (stateData.value?.viewFlipperValue != SETTINGS_MAIN) {
            stateData.value = stateData.value!!.copy(
                viewFlipperValue = SETTINGS_MAIN,
                showBackButton = false,
                toolbarRes = getToolbarRes(SETTINGS_MAIN)
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
            SecurityItems.PHONE -> {
                actionData.value =
                    SettingsAction.NavigateAction(SettingsFragmentDirections.settingsToDisplayPhone())
            }
            SecurityItems.PASS -> {
                actionData.value =
                    SettingsAction.NavigateAction(SettingsFragmentDirections.settingsToUpdatePassword())
            }
            SecurityItems.PIN -> {
                actionData.value =
                    SettingsAction.NavigateAction(SettingsFragmentDirections.settingsToPinCode())
            }
            SecurityItems.SEED -> {
                actionData.value = SettingsAction.NavigateAction(
                    SettingsFragmentDirections.toPassword(
                        R.id.password_to_create_seed_fragment,
                        R.string.seed_phrase_label,
                        CreateSeedFragment.MODE_SETTINGS
                    )
                )
            }
            SecurityItems.UNLINK -> {
                actionData.value = SettingsAction.NavigateAction(
                    SettingsFragmentDirections.settingsToUnlink()
                )
            }
        }
    }

    fun processArgs(settingsArgs: SettingsFragmentArgs) {
        stateData.value = stateData.value!!.copy(
            viewFlipperValue = settingsArgs.viewFlipperValue,
            showBackButton = settingsArgs.viewFlipperValue != 0,
            toolbarRes = getToolbarRes(settingsArgs.viewFlipperValue)
        )
    }

    private fun getToolbarRes(viewFlipperValue: Int): Int {
        return when (viewFlipperValue) {
            SETTINGS_SECURITY -> R.string.security_label
            SETTINGS_ABOUT -> R.string.about_label
            else -> R.string.settings
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
    val showBackButton: Boolean = false,
    val toolbarRes: Int = R.string.settings
)

sealed class SettingsAction {
    data class NavigateAction(val navDirections: NavDirections) : SettingsAction()
}