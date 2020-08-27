package com.app.belcobtm.presentation.features.settings.password

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import com.app.belcobtm.R
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.domain.authorization.interactor.CheckPassUseCase
import com.app.belcobtm.presentation.core.Const.MAX_PASS
import com.app.belcobtm.presentation.core.Const.MIN_PASS
import com.app.belcobtm.presentation.core.SingleLiveData
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.features.authorization.create.seed.CreateSeedFragment
import com.app.belcobtm.presentation.features.settings.SettingsFragment.Companion.SETTINGS_SECURITY

class PasswordViewModel(
    val checkPassUseCase: CheckPassUseCase,
    val prefsHelper: SharedPreferencesHelper
) : ViewModel() {

    val stateData =
        MutableLiveData<LoadingData<PasswordState>>(LoadingData.Success(PasswordState()))
    val actionData = SingleLiveData<PasswordAction>()
    lateinit var arguments: PasswordFragmentArgs

    fun passArgs(args: PasswordFragmentArgs) {
        arguments = args
    }

    fun onNextClick(pass: String) {
        stateData.value = LoadingData.Loading(data = stateData.value?.commonData)
        checkPassUseCase.invoke(CheckPassUseCase.Params(prefsHelper.userId.toString(), pass),
            onSuccess = {
                if (it) {
                    actionData.value = PasswordAction.NavigateAction(getDireciton())
                } else {
                    stateData.value = LoadingData.Error(data = stateData.value?.commonData)
                }
            },
            onError = {
                stateData.value = LoadingData.Error(data = stateData.value?.commonData, errorType = it)
            })
    }

    fun onTextChanged(text: String) {
        stateData.value =
            LoadingData.Success(
                stateData.value?.commonData?.copy(isButtonEnabled = text.length in MIN_PASS..MAX_PASS)
                    ?: PasswordState(isButtonEnabled = text.isNotEmpty())
            )
    }

    private fun getDireciton(): NavDirections {
        return when (arguments.destination) {
            R.id.password_to_create_seed_fragment -> PasswordFragmentDirections.passwordToCreateSeedFragment(
                mode = CreateSeedFragment.MODE_SETTINGS,
                seed = prefsHelper.apiSeed
            )
            R.id.password_to_change_phone_fragment -> PasswordFragmentDirections.passwordToChangePhoneFragment()
            else -> throw IllegalArgumentException("wrong direction passed")
        }
    }

    fun popBackStack() {
        when (arguments.destination) {
            R.id.password_to_create_seed_fragment -> actionData.value =
                PasswordAction.NavigateAction(
                    PasswordFragmentDirections.passwordToSettingsFragment(
                        SETTINGS_SECURITY
                    )
                )
            R.id.password_to_change_phone_fragment -> actionData.value =
                PasswordAction.BackStackAction
            else -> throw IllegalArgumentException("wrong direction passed")
        }
    }
}

data class PasswordState(val isButtonEnabled: Boolean = false)

sealed class PasswordAction {
    data class NavigateAction(val navDirections: NavDirections) : PasswordAction()
    object BackStackAction : PasswordAction()
}