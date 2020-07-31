package com.app.belcobtm.presentation.features.settings.phone

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import com.app.belcobtm.R
import com.app.belcobtm.domain.settings.interactor.UpdatePhoneUseCase
import com.app.belcobtm.presentation.core.SingleLiveData
import io.michaelrocks.libphonenumber.android.NumberParseException
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil

class PhoneChangeViewModel(
    private val changePhoneUseCase: UpdatePhoneUseCase,
    private val appContext: Context
) : ViewModel() {
    val stateData = MutableLiveData<PhoneChangeState>(PhoneChangeState.Ready())
    val actionData = SingleLiveData<PhoneChangeAction>()
    private var phone = ""
    private val phoneUtil: PhoneNumberUtil by lazy { PhoneNumberUtil.createInstance(appContext) }

    fun onPhoneInput(text: String) {
        phone = text
        stateData.value = PhoneChangeState.Ready(isValidMobileNumber(phone))
    }

    fun onNextClick() {
        changePhoneUseCase.invoke(
            UpdatePhoneUseCase.Params(phone),
            onSuccess = {
                if (it) {
                    actionData.value = PhoneChangeAction.NavigateAction(
                        PhoneChangeFragmentDirections.phoneChangeToSmsFragment(
                            phone,
                            R.id.sms_code_to_settings_fragment
                        )
                    )
                } else {
                    stateData.value = PhoneChangeState.Error
                }
            },
            onError = {
                stateData.value = PhoneChangeState.Error
            })
    }

    private fun isValidMobileNumber(phone: String): Boolean = if (phone.isNotBlank()) {
        try {
            val number = PhoneNumberUtil.createInstance(appContext).parse(phone, "")
            phoneUtil.isValidNumber(number)
        } catch (e: NumberParseException) {
            false
        }
    } else {
        false
    }
}

sealed class PhoneChangeState {
    object Loading : PhoneChangeState()
    object Error : PhoneChangeState()
    data class Ready(val isNextButtonEnabled: Boolean = false) : PhoneChangeState()
}

sealed class PhoneChangeAction {
    class NavigateAction(val navDirections: NavDirections) : PhoneChangeAction()
}