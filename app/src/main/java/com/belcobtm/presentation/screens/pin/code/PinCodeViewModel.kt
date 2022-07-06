package com.belcobtm.presentation.screens.pin.code

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.belcobtm.R
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.authorization.interactor.AuthorizeUseCase
import com.belcobtm.domain.authorization.interactor.GetAuthorizePinUseCase
import com.belcobtm.domain.authorization.interactor.SaveAuthorizePinUseCase
import com.belcobtm.domain.authorization.interactor.SaveUserAuthedUseCase
import com.belcobtm.domain.settings.interactor.BioAuthAllowedByUserUseCase
import com.belcobtm.domain.settings.interactor.BioAuthSupportedByPhoneUseCase
import com.belcobtm.domain.settings.interactor.UnlinkUseCase
import com.belcobtm.domain.support.SupportChatInteractor
import com.belcobtm.presentation.core.SingleLiveData
import com.belcobtm.presentation.screens.pin.code.PinCodeFragment.Companion.KEY_PIN_MODE_CHANGE
import com.belcobtm.presentation.screens.pin.code.PinCodeFragment.Companion.KEY_PIN_MODE_CREATE
import com.belcobtm.presentation.screens.pin.code.PinCodeFragment.Companion.KEY_PIN_MODE_ENTER
import com.belcobtm.presentation.screens.pin.code.PinCodeFragment.Companion.PIN_CODE_LENGTH
import com.belcobtm.presentation.screens.pin.code.PinCodeFragment.Companion.STEP_CONFIRM
import com.belcobtm.presentation.screens.pin.code.PinCodeFragment.Companion.STEP_CREATE
import com.belcobtm.presentation.screens.pin.code.PinCodeFragment.Companion.STEP_VERIFY

class PinCodeViewModel(
    private val authorizeUseCase: AuthorizeUseCase,
    private val unlinkUseCase: UnlinkUseCase,
    private val bioAuthSupportedByPhoneUseCase: BioAuthSupportedByPhoneUseCase,
    private val bioAuthAllowedByUserUseCase: BioAuthAllowedByUserUseCase,
    private val authorizePinUseCase: GetAuthorizePinUseCase,
    private val savePinCodeUseCase: SaveAuthorizePinUseCase,
    private val saveUserAuthedUseCase: SaveUserAuthedUseCase,
    supportChatInteractor: SupportChatInteractor
) : ViewModel() {

    val stateData = MutableLiveData(PinCodeState())
    val actionData = SingleLiveData<PinCodeAction>()

    private val _bioAuthVisible = MutableLiveData(false)
    val bioAuthVisible: LiveData<Boolean> = _bioAuthVisible

    private var enteredPin = ""

    private var mode = KEY_PIN_MODE_ENTER
    private var step = STEP_CREATE
    private var currentPin = ""
    private var isError = false

    init {
        supportChatInteractor.init()
    }

    private fun savePinCode(pinCode: String) =
        savePinCodeUseCase.invoke(SaveAuthorizePinUseCase.Params(pinCode))

    fun saveUserAuthed(userAuthed: Boolean) =
        saveUserAuthedUseCase.invoke(SaveUserAuthedUseCase.Params(userAuthed))

    private fun getSavedPinCode(): String = authorizePinUseCase.invoke()

    fun authorize() {
        stateData.value = stateData.value!!.copy(isLoading = true)
        Log.d("REFRESH", "From PinCodeViewModel")
        authorizeUseCase.invoke(
            Unit,
            onSuccess = { actionData.value = PinCodeAction.Success },
            onError = {
                if (it is Failure.TokenError) {
                    // in case we faced with a TokenError during refreshing
                    // we should start an unlink flow
                    unlinkAccountAndStartAuthorization()
                } else {
                    enteredPin = ""
                    currentPin = ""
                    updateState()
                    actionData.value = PinCodeAction.AuthorizeError(it)
                }
            }
        )
    }

    fun setMode(mode: String) {
        this.mode = mode
        when (mode) {
            KEY_PIN_MODE_ENTER -> {
                step = STEP_VERIFY
                checkForBioAuth()
            }
            KEY_PIN_MODE_CHANGE -> step = STEP_VERIFY
            KEY_PIN_MODE_CREATE -> step - STEP_CREATE
        }
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

    private fun onPinEntered() {
        when {
            mode == KEY_PIN_MODE_ENTER -> validateUsualPinEnter()
            step == STEP_CREATE -> saveFirstPinVersion()
            step == STEP_CONFIRM -> validateConfirmedPin()
            step == STEP_VERIFY && mode == KEY_PIN_MODE_CHANGE -> validateOnChange()
            else -> throw IllegalStateException("Wrong logic case, fix on dev side")
        }
    }

    fun onBackClick() {
        if (step > STEP_VERIFY) {
            currentPin = ""
            enteredPin = ""
            isError = false
            step--
            updateState()
        } else {
            actionData.postValue(PinCodeAction.BackPress)
        }
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
        when (mode) {
            KEY_PIN_MODE_ENTER, KEY_PIN_MODE_CREATE ->
                actionData.value = PinCodeAction.Success
            KEY_PIN_MODE_CHANGE ->
                actionData.value = PinCodeAction.ChangedPin
        }
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
            backButtonVisible = mode == KEY_PIN_MODE_CHANGE || step == STEP_CONFIRM,
            showMenu = mode == KEY_PIN_MODE_CHANGE
        ) ?: PinCodeState()
    }

    private fun getTitleRes(): Int {
        return when {
            mode == KEY_PIN_MODE_ENTER -> R.string.pin_code_screen_enter_pin
            mode == KEY_PIN_MODE_CREATE && step == STEP_CREATE -> R.string.setup_pin_code
            mode == KEY_PIN_MODE_CREATE && step == STEP_CONFIRM -> R.string.pin_code_screen_new_pin_confirm
            mode == KEY_PIN_MODE_CHANGE && step == STEP_VERIFY -> R.string.pin_code_screen_old_pin
            mode == KEY_PIN_MODE_CHANGE && step == STEP_CREATE -> R.string.pin_code_screen_new_pin
            mode == KEY_PIN_MODE_CHANGE && step == STEP_CONFIRM -> R.string.pin_code_screen_new_pin_confirm
            else -> throw IllegalStateException("Wrong logic case, fix on dev side")
        }
    }

    private fun unlinkAccountAndStartAuthorization() {
        unlinkUseCase(
            Unit,
            onSuccess = { actionData.value = PinCodeAction.StartWelcomeScreen },
            onError = { actionData.value = PinCodeAction.StartWelcomeScreen }
        )
    }

    private fun checkForBioAuth() {
        bioAuthSupportedByPhoneUseCase(
            UseCase.None(),
            onSuccess = { supported ->
                if (supported)
                    bioAuthAllowedByUserUseCase(
                        UseCase.None(),
                        onSuccess = { allowed ->
                            if (allowed) actionData.value = PinCodeAction.StartBioPromt
                            _bioAuthVisible.value = allowed
                        },
                        onError = {}
                    )
            },
            onError = {},
        )
    }

}

data class PinCodeState(
    val visiblePin: String = "",
    val labelResource: Int = R.string.pin_code_screen_enter_pin,
    val isError: Boolean = false,
    val backButtonVisible: Boolean = false,
    val isLoading: Boolean = false,
    val showMenu: Boolean = false
)

sealed class PinCodeAction {
    data class Vibrate(val duration: Long) : PinCodeAction()
    object Success : PinCodeAction()
    object ChangedPin : PinCodeAction()
    data class AuthorizeError(val failure: Failure) : PinCodeAction()
    object BackPress : PinCodeAction()
    object StartBioPromt : PinCodeAction()
    object StartWelcomeScreen : PinCodeAction()
}
