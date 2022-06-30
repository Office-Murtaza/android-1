package com.belcobtm.presentation.screens.settings.security

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import com.belcobtm.R
import com.belcobtm.domain.PreferencesInteractor
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.settings.interactor.BioAuthAllowedByUserUseCase
import com.belcobtm.domain.settings.interactor.BioAuthSupportedByPhoneUseCase
import com.belcobtm.domain.settings.interactor.SetBioAuthStateAllowedUseCase
import com.belcobtm.domain.settings.interactor.UpdatePhoneUseCase
import com.belcobtm.presentation.core.SingleLiveData
import com.belcobtm.presentation.screens.authorization.create.seed.CreateSeedFragment
import com.belcobtm.presentation.tools.formatter.PhoneNumberFormatter

class SecurityViewModel(
    private val phoneNumberFormatter: PhoneNumberFormatter,
    private val setBioAuthStateAllowedUseCase: SetBioAuthStateAllowedUseCase,
    private val bioAuthAllowedByUserUseCase: BioAuthAllowedByUserUseCase,
    private val bioAuthSupportedByPhoneUseCase: BioAuthSupportedByPhoneUseCase,
    private val updatePhoneUseCase: UpdatePhoneUseCase,
    private val preferences: PreferencesInteractor
) : ViewModel() {

    private val _actionData = SingleLiveData<SecurityAction>()
    val actionData: LiveData<SecurityAction> = _actionData

    private val _userPhone = MutableLiveData<String>()
    val userPhone: LiveData<String> = _userPhone

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
        _userPhone.value = phoneNumberFormatter.format(preferences.userPhone)
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
