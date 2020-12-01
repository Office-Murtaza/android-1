package com.app.belcobtm.presentation.features.settings.security

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import com.app.belcobtm.R
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.settings.interactor.GetPhoneUseCase
import com.app.belcobtm.presentation.core.SingleLiveData
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.features.authorization.create.seed.CreateSeedFragment

class SecurityViewModel(private val getPhoneUseCase: GetPhoneUseCase) : ViewModel() {

    private val _actionData = SingleLiveData<SecurityAction>()
    val actionData: LiveData<SecurityAction> = _actionData

    private val _userPhone = MutableLiveData<LoadingData<String>>()
    val userPhone: LiveData<LoadingData<String>> = _userPhone

    init {
        fetchUserPhone()
    }

    fun handleItemClick(securityItem: SecurityItem) {
        val direction = when (securityItem) {
            SecurityItem.PHONE -> {
                SecurityFragmentDirections.toPassword(
                    R.id.password_to_change_phone_fragment,
                    R.string.update_phone_label
                )
            }
            SecurityItem.PASS -> {
                SecurityFragmentDirections.toUpdatePassword()
            }
            SecurityItem.PIN -> {
                SecurityFragmentDirections.toPinCode()
            }
            SecurityItem.SEED -> {
                SecurityFragmentDirections.toPassword(
                    R.id.password_to_create_seed_fragment,
                    R.string.seed_phrase_label,
                    CreateSeedFragment.MODE_SETTINGS
                )
            }
            SecurityItem.UNLINK -> {
                SecurityFragmentDirections.toUnlink()
            }
        }
        _actionData.value = SecurityAction.NavigateAction(direction)
    }

    private fun fetchUserPhone() {
        _userPhone.value = LoadingData.Loading()
        getPhoneUseCase(
            UseCase.None(),
            onSuccess = { _userPhone.value = LoadingData.Success(it) },
            onError = { _userPhone.value = LoadingData.Error(it) }
        )
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
}