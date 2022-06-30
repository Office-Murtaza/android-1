package com.belcobtm.presentation.screens.settings.security.password

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import com.belcobtm.R
import com.belcobtm.domain.PreferencesInteractor
import com.belcobtm.domain.authorization.interactor.CheckPassUseCase
import com.belcobtm.presentation.core.Const.MAX_PASS
import com.belcobtm.presentation.core.Const.MIN_PASS
import com.belcobtm.presentation.core.SingleLiveData
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.screens.authorization.create.seed.CreateSeedFragment

class PasswordViewModel(
    private val checkPassUseCase: CheckPassUseCase,
    private val preferences: PreferencesInteractor
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
        checkPassUseCase.invoke(CheckPassUseCase.Params(preferences.userId, pass),
            onSuccess = {
                if (it) {
                    actionData.value = PasswordAction.NavigateAction(getDirection())
                } else {
                    stateData.value = LoadingData.Error(data = stateData.value?.commonData)
                }
            },
            onError = {
                stateData.value =
                    LoadingData.Error(data = stateData.value?.commonData, errorType = it)
            })
    }

    fun onTextChanged(text: String) {
        stateData.value =
            LoadingData.Success(
                stateData.value?.commonData?.copy(isButtonEnabled = text.length in MIN_PASS..MAX_PASS)
                    ?: PasswordState(isButtonEnabled = text.isNotEmpty())
            )
    }

    private fun getDirection(): NavDirections {
        return when (arguments.destination) {
            R.id.password_to_create_seed_fragment -> PasswordFragmentDirections.passwordToCreateSeedFragment(
                mode = CreateSeedFragment.MODE_SETTINGS,
                seed = preferences.apiSeed
            )
            else -> throw IllegalArgumentException("wrong direction passed")
        }
    }

    fun popBackStack() {
        when (arguments.destination) {
            R.id.password_to_create_seed_fragment -> actionData.value =
                PasswordAction.PopToSecurityAction
            else -> throw IllegalArgumentException("wrong direction passed")
        }
    }
}

data class PasswordState(val isButtonEnabled: Boolean = false)

sealed class PasswordAction {
    data class NavigateAction(val navDirections: NavDirections) : PasswordAction()
    object BackStackAction : PasswordAction()
    object PopToSecurityAction : PasswordAction()
}
