package com.app.belcobtm.presentation.features.authorization.recover.wallet

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.authorization.interactor.RecoverWalletUseCase
import com.app.belcobtm.domain.authorization.interactor.VerifySmsCodeUseCase
import com.app.belcobtm.presentation.core.mvvm.LoadingData

class RecoverWalletViewModel(
    private val recoverWalletUseCase: RecoverWalletUseCase,
    private val smsCodeUseCase: VerifySmsCodeUseCase
) : ViewModel() {
    val recoverWalletLiveData: MutableLiveData<LoadingData<Unit>> = MutableLiveData()
    val smsCodeLiveData: MutableLiveData<LoadingData<Unit>> = MutableLiveData()

    fun recoverWallet(phone: String, password: String) {
        recoverWalletLiveData.value = LoadingData.Loading()
        recoverWalletUseCase.invoke(RecoverWalletUseCase.Params(phone, password)) { either ->
            either.either(
                { recoverWalletLiveData.value = LoadingData.Error(it) },
                { recoverWalletLiveData.value = LoadingData.Success(Unit) }
            )
        }


//        showProgress(true)
//        mDataManager.recoverWallet(phone, password)
//            .flatMap { response ->
//                App.appContext().pref.setSessionApiToken(response.value?.accessToken)
//                App.appContext().pref.setRefreshApiToken(response.value?.refreshToken)
//                App.appContext().pref.setUserId(response.value?.userId)
//                mDataManager.updateToken()
//
//                return@flatMap Observable.just(response)
//            }
//            .subscribe({ response: Optional<AuthResponse> ->
//                showSmsCodeDialog()
//                showProgress(false)
//                userId = response.value?.userId.toString()
//            }
//                , { error: Throwable ->
//                    showProgress(false)
//                    if (error is ServerException) {
//                        showError(error.errorMessage)
//                    } else {
//                        showError(error.message)
//                    }
//                })
    }

    fun verifySmsCode(smsCode: String) {
        smsCodeLiveData.value = LoadingData.Loading()
        smsCodeUseCase.invoke(VerifySmsCodeUseCase.Params(smsCode)) { either ->
            either.either(
                { smsCodeLiveData.value = LoadingData.Error(it) },
                { smsCodeLiveData.value = LoadingData.Success(Unit) }
            )
        }
    }

}