package com.belcobtm.presentation.features.settings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import com.belcobtm.domain.settings.interactor.GetVerificationInfoUseCase
import com.belcobtm.domain.settings.type.VerificationStatus
import com.belcobtm.domain.settings.type.isVerified
import com.belcobtm.presentation.core.SingleLiveData
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.features.settings.SettingsFragment.Companion.SETTINGS_ABOUT
import com.belcobtm.presentation.features.settings.SettingsFragment.Companion.SETTINGS_MAIN
import com.belcobtm.presentation.features.settings.SettingsFragment.Companion.SETTINGS_SECURITY


class SettingsViewModel(private val getVerificationInfoUseCase: GetVerificationInfoUseCase) : ViewModel() {

    val actionData = SingleLiveData<SettingsAction>()
    val stateData = MutableLiveData<LoadingData<VerificationStatus>>()

    init {
        getVerificationInfoUseCase.invoke(Unit,
            onSuccess = {
                stateData.value = LoadingData.Success(
                    it.status
                )
            },
            onError = {
                stateData.value = LoadingData.Error(it)
            }
        )
    }

    fun navigateToKYC() {
        val dest = SettingsFragmentDirections.settingsToVerificationInfoFragment()
        actionData.value = SettingsAction.NavigateAction(dest)
    }

    fun onSectionClick(section: SettingsSections) {
        stateData.value?.let {
            if (it is LoadingData.Success && it.data.isVerified()) {
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
                        navigateToKYC()
                    }
                    SettingsSections.SUPPORT -> {
                        val dest = SettingsFragmentDirections.settingsToSupportFragment()
                        actionData.value = SettingsAction.NavigateAction(dest)
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
            } else {
                actionData.value = SettingsAction.ShowVerifyDialog
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
    object ShowVerifyDialog: SettingsAction()
}


