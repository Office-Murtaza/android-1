package com.app.belcobtm.presentation.features.pin.code

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.R
import com.app.belcobtm.domain.authorization.interactor.AuthorizeUseCase
import com.app.belcobtm.domain.authorization.interactor.GetAuthorizePinUseCase
import com.app.belcobtm.domain.authorization.interactor.SaveAuthorizePinUseCase
import com.app.belcobtm.presentation.core.SingleLiveData
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.features.pin.code.PinCodeFragment.Companion.KEY_PIN_MODE_CHANGE
import com.app.belcobtm.presentation.features.pin.code.PinCodeFragment.Companion.KEY_PIN_MODE_CREATE
import com.app.belcobtm.presentation.features.pin.code.PinCodeFragment.Companion.KEY_PIN_MODE_ENTER
import com.app.belcobtm.presentation.features.pin.code.PinCodeFragment.Companion.PIN_CODE_LENGTH
import com.app.belcobtm.presentation.features.pin.code.PinCodeFragment.Companion.STEP_CONFIRM
import com.app.belcobtm.presentation.features.pin.code.PinCodeFragment.Companion.STEP_CREATE
import com.app.belcobtm.presentation.features.pin.code.PinCodeFragment.Companion.STEP_VERIFY

class PinCodeViewModel(
    private val authorizeUseCase: AuthorizeUseCase,
    private val authorizePinUseCase: GetAuthorizePinUseCase,
    private val savePinCodeUseCase: SaveAuthorizePinUseCase
) : ViewModel() {
    fun savePinCode(pinCode: String) =
        savePinCodeUseCase.invoke(SaveAuthorizePinUseCase.Params(pinCode))

    fun getSavedPinCode(): String = authorizePinUseCase.invoke()

    fun authorize() {
        stateData.value = stateData.value!!.copy(isLoading = true)
        authorizeUseCase.invoke(
            Unit,
            onSuccess = { actionData.value = PinCodeAction.Success },
            onError = {
                enteredPin = ""
                currentPin = ""
                updateState()
                actionData.value = PinCodeAction.AuthorizeError
            }
        )
    }

    val stateData = MutableLiveData<PinCodeState>(PinCodeState())
    val actionData = SingleLiveData<PinCodeAction>()

    private var enteredPin = ""

    private var mode = KEY_PIN_MODE_ENTER
    private var step = STEP_VERIFY
    private var currentPin = ""
    private var isError = false

    fun setMode(mode: String) {
        this.mode = mode
        updateState()
    }

    fun onAddPinSymbol(symbol: String) {
        isError = false
        currentPin += symbol
        if (currentPin.length > PIN_CODE_LENGTH) {
            currentPin = ""
            updateState()
        }
        if (currentPin.length == PIN_CODE_LENGTH) {
            updateState()
            onPinEntered()
        } else {
            updateState()
        }
    }

    fun onRemoveSymbol() {
        if (currentPin.isNotEmpty()) {
            currentPin = currentPin.substring(0, currentPin.length - 1)
            updateState()
        }
    }

    fun onPinEntered() {
        when {
            step == STEP_CREATE -> saveFirstPinVersion()
            step == STEP_CONFIRM -> validateConfirmedPin()
            step == STEP_VERIFY && mode == KEY_PIN_MODE_ENTER -> validateUsualPinEnter()
            step == STEP_VERIFY && mode == KEY_PIN_MODE_CHANGE -> validateOnChange()
            else -> throw IllegalStateException("Wrong logic case, fix on dev side")
        }
    }

    fun onBackClick() {
        currentPin = ""
        enteredPin = ""
        isError = false
        step = STEP_CREATE
        updateState()
    }

    private fun validateOnChange() {
        if (currentPin == getSavedPinCode()) {
            verifiedOnChange()
        } else {
            currentPin = ""
            wrongPin()
        }
    }

    private fun validateUsualPinEnter() {
        if (currentPin == getSavedPinCode()) {
            vibrate(100)
            authorize()
        } else {
            currentPin = ""
            wrongPin()
        }
    }

    private fun verifiedOnChange() {
        currentPin = ""
        step = STEP_CREATE
        vibrate(100)
        updateState()
    }

    private fun saveFirstPinVersion() {
        enteredPin = currentPin
        currentPin = ""
        step = STEP_CONFIRM
        vibrate(100)
        updateState()
    }

    private fun validateConfirmedPin() {
        if (enteredPin == currentPin) {
            savePinCode(currentPin)
            matchedPin()
        } else {
            step = STEP_CREATE
            currentPin = ""
            wrongPin()
        }
    }

    private fun matchedPin() {
        actionData.value = PinCodeAction.Success
    }

    private fun wrongPin() {
        isError = true
        updateState()
    }

    private fun vibrate(duration: Long) {
        actionData.value = PinCodeAction.Vibrate(duration)
    }

    private fun updateState() {
        stateData.value = stateData.value?.copy(
            visiblePin = currentPin,
            labelResource = getTitleRes(),
            isError = isError,
            backButtonVisible = mode != KEY_PIN_MODE_ENTER && currentPin.length == PIN_CODE_LENGTH
        ) ?: PinCodeState()
    }

    private fun getTitleRes(): Int {
        return when {
            mode == KEY_PIN_MODE_ENTER -> R.string.pin_code_screen_enter_pin
            mode == KEY_PIN_MODE_CREATE && step == STEP_CREATE -> R.string.setup_pin_code
            mode == KEY_PIN_MODE_CREATE && step == STEP_CONFIRM -> R.string.pin_code_screen_confirm_pin
            mode == KEY_PIN_MODE_CHANGE && step == STEP_VERIFY -> R.string.pin_code_screen_old_pin
            mode == KEY_PIN_MODE_CHANGE && step == STEP_CREATE -> R.string.pin_code_screen_new_pin
            mode == KEY_PIN_MODE_CHANGE && step == STEP_CONFIRM -> R.string.pin_code_screen_new_pin_confirm
            else -> throw IllegalStateException("Wrong logic case, fix on dev side")
        }
    }
}

data class PinCodeState(
    val visiblePin: String = "",
    val labelResource: Int = R.string.pin_code_screen_enter_pin,
    val isError: Boolean = false,
    val backButtonVisible: Boolean = false,
    val isLoading: Boolean = false
)

sealed class PinCodeAction {
    data class Vibrate(val duration: Long) : PinCodeAction()
    object Success : PinCodeAction()
    object ChangedPin : PinCodeAction()
    object AuthorizeError : PinCodeAction()
}