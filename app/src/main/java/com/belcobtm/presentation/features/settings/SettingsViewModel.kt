package com.belcobtm.presentation.features.settings

import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import com.belcobtm.domain.settings.interactor.UpdatePhoneUseCase
import com.belcobtm.presentation.core.SingleLiveData
import com.belcobtm.presentation.features.settings.SettingsFragment.Companion.SETTINGS_ABOUT
import com.belcobtm.presentation.features.settings.SettingsFragment.Companion.SETTINGS_MAIN
import com.belcobtm.presentation.features.settings.SettingsFragment.Companion.SETTINGS_SECURITY

class SettingsViewModel(
    private val updatePhoneUseCase: UpdatePhoneUseCase
) : ViewModel() {

    val actionData = SingleLiveData<SettingsAction>()

    fun onSectionClick(section: SettingsSections) {
        when (section) {
            SettingsSections.WALLETS -> {
                val dest = SettingsFragmentDirections.settingsToManageFragment()
                actionData.value = SettingsAction.NavigateAction(dest)
            }
            SettingsSections.SECURITY -> {
                val dest = SettingsFragmentDirections.settingsToSecurityFragment()
                actionData.value = SettingsAction.NavigateAction(dest)
            }
            SettingsSections.KYC -> {
                val dest = SettingsFragmentDirections.settingsToVerificationInfoFragment()
                actionData.value = SettingsAction.NavigateAction(dest)
            }
            SettingsSections.SUPPORT -> {
                actionData.value = SettingsAction.SupportChat
            }
            SettingsSections.ABOUT -> {
                val dest = SettingsFragmentDirections.settingsToAboutFragment()
                actionData.value = SettingsAction.NavigateAction(dest)
            }
            SettingsSections.NOTIFICATIONS -> {
                actionData.value = SettingsAction.NotificationOptions
            }
            SettingsSections.REFERRALS -> {
                val dest = SettingsFragmentDirections.settingsToReferralsFragment()
                actionData.value = SettingsAction.NavigateAction(dest)
            }
        }
    }

    fun processArgs(settingsArgs: SettingsFragmentArgs) {
        when (settingsArgs.viewFlipperValue) {
            SETTINGS_MAIN -> {
                // noop. we are already here
            }
            SETTINGS_ABOUT -> {
                val destination = SettingsFragmentDirections.settingsToAboutFragment()
                actionData.value = SettingsAction.NavigateAction(destination)
            }
            SETTINGS_SECURITY -> {
                val destination = SettingsFragmentDirections.settingsToSecurityFragment()
                actionData.value = SettingsAction.NavigateAction(destination)
            }
        }
    }

    fun updatePhone() {
        updatePhoneUseCase.invoke(
            Unit,
            onSuccess = {
                actionData.value = SettingsAction.PhoneSuccessfullyUpdated
            },
            onError = {
                actionData.value = SettingsAction.PhoneUpdateFailed
            })
    }
}

enum class SettingsSections {
    WALLETS,
    SECURITY,
    KYC,
    NOTIFICATIONS,
    SUPPORT,
    ABOUT,
    REFERRALS,
}

sealed class SettingsAction {
    object NotificationOptions : SettingsAction()
    data class NavigateAction(val navDirections: NavDirections) : SettingsAction()
    object SupportChat : SettingsAction()
    object PhoneSuccessfullyUpdated : SettingsAction()
    object PhoneUpdateFailed : SettingsAction()
}
