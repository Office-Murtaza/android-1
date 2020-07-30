package com.app.belcobtm.presentation.features.settings.password

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import com.app.belcobtm.R
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.domain.authorization.interactor.CheckPassUseCase
import com.app.belcobtm.presentation.core.SingleLiveData
import com.app.belcobtm.presentation.features.authorization.create.seed.CreateSeedFragment

class PasswordViewModel(
    val checkPassUseCase: CheckPassUseCase,
    val prefsHelper: SharedPreferencesHelper
) : ViewModel() {

    val stateData = MutableLiveData<PasswordState>(PasswordState.Ready())
    val actionData = SingleLiveData<PasswordAction>()
    lateinit var arguments: PasswordFragmentArgs

    fun passArgs(args: PasswordFragmentArgs) {
        arguments = args
    }

    fun onNextClick(pass: String) {
        stateData.value = PasswordState.Loading
        checkPassUseCase.invoke(CheckPassUseCase.Params(prefsHelper.userId.toString(), pass),
        onSuccess = {
            actionData.value = PasswordAction.NavigateAction(getDireciton())
        },
        onError = {
            stateData.value = PasswordState.Ready(isButtonEnabled = false, isError = true, errorText = "Password is wrong")
        })
    }

    fun onTextChanged(text: String) {
        stateData.value = PasswordState.Ready(isButtonEnabled = text.isNotEmpty())
    }

    private fun getDireciton(): NavDirections {
        return when (arguments.destination) {
            R.id.password_to_create_seed_fragment -> PasswordFragmentDirections.passwordToCreateSeedFragment(CreateSeedFragment.MODE_SETTINGS)
            R.id.password_to_change_phone_fragment -> PasswordFragmentDirections.passwordToChangePhoneFragment()
            R.id.password_to_unlink_fragment -> PasswordFragmentDirections.passwordToUnlinkFragment()
            else -> throw IllegalArgumentException("wrong direction passed")
        }
    }
}

sealed class PasswordState {
    data class Ready(val isButtonEnabled: Boolean = false, val isError: Boolean = false, val errorText: String? = null): PasswordState()
    object Loading: PasswordState()
}

sealed class PasswordAction {
    data class NavigateAction(val navDirections: NavDirections): PasswordAction()
}