package com.app.belcobtm.presentation.features.settings.phone

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.settings.interactor.ERROR_UPDATE_PHONE_IS_SAME
import com.app.belcobtm.domain.settings.interactor.ERROR_UPDATE_PHONE_IS_USED
import com.app.belcobtm.domain.settings.interactor.VerifyPhoneUseCase
import com.app.belcobtm.presentation.core.SingleLiveData
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import io.michaelrocks.libphonenumber.android.NumberParseException
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil

class PhoneChangeViewModel(
    private val verifyPhoneUseCase: VerifyPhoneUseCase,
    private val appContext: Context,
    private val prefsHelper: SharedPreferencesHelper
) : ViewModel() {
    val stateData =
        MutableLiveData<LoadingData<PhoneChangeState>>(LoadingData.Success(PhoneChangeState()))
    val actionData = SingleLiveData<PhoneChangeAction>()
    private var phone = ""
    private val phoneUtil: PhoneNumberUtil by lazy { PhoneNumberUtil.createInstance(appContext) }

    fun onPhoneInput(text: String) {
        phone = text
        stateData.value = LoadingData.Success(PhoneChangeState(isValidMobileNumber(phone)))
    }

    fun onNextClick() {
        if (phone == prefsHelper.userPhone) {
            stateData.value = LoadingData.Error(
                data = stateData.value?.commonData,
                errorType = Failure.MessageError(code = ERROR_UPDATE_PHONE_IS_SAME, message = null)
            )
            return
        }
        verifyPhoneUseCase.invoke(
            VerifyPhoneUseCase.Params(phone),
            onSuccess = {
                if (it) {
                    stateData.value = LoadingData.Error(
                        data = stateData.value?.commonData,
                        errorType = Failure.MessageError(
                            code = ERROR_UPDATE_PHONE_IS_USED,
                            message = null
                        )
                    )
                } else {
                    actionData.value = PhoneChangeAction.PopBackStackToSecurity
                }
            },
            onError = {
                stateData.value =
                    LoadingData.Error(data = stateData.value?.commonData, errorType = it)
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

data class PhoneChangeState(
    val isNextButtonEnabled: Boolean = false,
    val isPhoneError: Boolean = false
)

sealed class PhoneChangeAction {
    object PopBackStackToSecurity : PhoneChangeAction()
    class NavigateAction(val navDirections: NavDirections) : PhoneChangeAction()
}