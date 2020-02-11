package com.app.belcobtm.presentation.features.authorization.recover.wallet

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.presentation.core.mvvm.LoadingData

class RecoverWalletViewModel : ViewModel() {
    val recoverWalletLiveData: MutableLiveData<LoadingData<Unit>> = MutableLiveData()

    init {

    }

    fun recoverWallet(phone: String, password: String) {

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

}