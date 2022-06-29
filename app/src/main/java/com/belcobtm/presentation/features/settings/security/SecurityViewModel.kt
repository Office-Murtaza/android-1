package com.belcobtm.presentation.features.settings.security

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import com.belcobtm.R
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.settings.interactor.BioAuthAllowedByUserUseCase
import com.belcobtm.domain.settings.interactor.BioAuthSupportedByPhoneUseCase
import com.belcobtm.domain.settings.interactor.GetPhoneUseCase
import com.belcobtm.domain.settings.interactor.SetBioAuthStateAllowedUseCase
import com.belcobtm.domain.settings.interactor.UpdatePhoneUseCase
import com.belcobtm.presentation.core.SingleLiveData
import com.belcobtm.presentation.core.formatter.PhoneNumberFormatter
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.features.authorization.create.seed.CreateSeedFragment

class SecurityViewModel(
    private val getPhoneUseCase: GetPhoneUseCase,
    private val phoneNumberFormatter: PhoneNumberFormatter,
    private val setBioAuthStateAllowedUseCase: SetBioAuthStateAllowedUseCase,
    private val bioAuthAllowedByUserUseCase: BioAuthAllowedByUserUseCase,
    private val bioAuthSupportedByPhoneUseCase: BioAuthSupportedByPhoneUseCase,
    private val updatePhoneUseCase: UpdatePhoneUseCase
) : ViewModel() {

    private val _actionData = SingleLiveData<SecurityAction>()
    val actionData: LiveData<SecurityAction> = _actionData

    private val _userPhone = MutableLiveData<LoadingData<String>>()
    val userPhone: LiveData<LoadingData<String>> = _userPhone

    private val _bioOption = MutableLiveData<BioOptionSwitch>()
    val bioOption: LiveData<BioOptionSwitch> = _bioOption

    init {
        fetchUserPhone()
        checkAndSetBioState()
    }

    fun invertBioAuth() {
        val currentState = bioOption.value ?: return
        val newState = currentState.allowed.not()
        setBioAuthStateAllowedUseCase(
            SetBioAuthStateAllowedUseCase.Params(newState),
            onSuccess = { _bioOption.value = currentState.copy(allowed = newState) },
            onError = {}
        )
    }

    fun handleItemClick(securityItem: SecurityItem) {
        _actionData.value = SecurityAction.NavigateAction(
            when (securityItem) {
                SecurityItem.PHONE -> SecurityFragmentDirections.toChangePhoneFragment()
                SecurityItem.PASS -> SecurityFragmentDirections.toUpdatePassword()
                SecurityItem.PIN -> SecurityFragmentDirections.toPinCode()
                SecurityItem.SEED -> SecurityFragmentDirections.toPassword(
                    R.id.password_to_create_seed_fragment,
                    R.string.seed_phrase_label,
                    CreateSeedFragment.MODE_SETTINGS
                )
                SecurityItem.UNLINK -> SecurityFragmentDirections.toUnlink()
            }
        )
    }

    private fun fetchUserPhone() {
        _userPhone.value = LoadingData.Loading()
        getPhoneUseCase(
            UseCase.None(),
            onSuccess = {
                val formattedNumber = phoneNumberFormatter.format(it)
                _userPhone.value = LoadingData.Success(formattedNumber)
            },
            onError = { _userPhone.value = LoadingData.Error(it) }
        )
    }

    private fun checkAndSetBioState() {
        bioAuthSupportedByPhoneUseCase(
            UseCase.None(),
            onSuccess = { supported ->
                if (supported.not()) {
                    _bioOption.value = BioOptionSwitch(supported, false)
                } else {
                    bioAuthAllowedByUserUseCase(
                        UseCase.None(),
                        onSuccess = { allowed ->
                            _bioOption.value = BioOptionSwitch(supported, allowed)
                        },
                        onError = {}
                    )
                }
            },
            onError = {}
        )
    }

    fun updatePhone() {
        updatePhoneUseCase.invoke(
            Unit,
            onSuccess = {
                fetchUserPhone()
                _actionData.value = SecurityAction.PhoneSuccessfullyUpdated
            },
            onError = {
                _actionData.value = SecurityAction.PhoneUpdateFailed
            })
    }

}

enum class SecurityItem {
    PHONE,
    PASS,
    PIN,
    SEED,
    UNLINK
}

sealed class SecurityAction {
    data class NavigateAction(val navDirections: NavDirections) : SecurityAction()
    object PhoneSuccessfullyUpdated : SecurityAction()
    object PhoneUpdateFailed : SecurityAction()
}

data class BioOptionSwitch(val supported: Boolean, val allowed: Boolean)
