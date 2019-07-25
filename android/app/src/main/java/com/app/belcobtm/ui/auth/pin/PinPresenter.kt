package com.app.belcobtm.ui.auth.pin

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import com.app.belcobtm.App
import com.app.belcobtm.api.data_manager.AuthDataManager
import com.app.belcobtm.api.model.ServerException
import com.app.belcobtm.mvp.BaseMvpDIPresenterImpl
import com.app.belcobtm.util.Const.ERROR_403
import com.app.belcobtm.util.pref


class PinPresenter : BaseMvpDIPresenterImpl<PinContract.View, AuthDataManager>(), PinContract.Presenter {

    override fun injectDependency() {
        presenterComponent.inject(this)
    }

    override fun savePin(pin: String) {
        App.appContext().pref.setPin(pin)
    }

    override fun checkCryptoPin(pin: String) {
        val savedPin = App.appContext().pref.getPin()
        if (savedPin == pin) {
            vibrate(50)
            updateToken()
        } else {
            mView?.pinNotMatch()
        }
    }

    private fun updateToken() {
        mView?.showProgress(true)
        mDataManager.refreshToken(App.appContext().pref.getRefreshApiToken())
            .subscribe({ response ->
                App.appContext().pref.setSessionApiToken(response.value?.accessToken)
                App.appContext().pref.setRefreshApiToken(response.value?.refreshToken)
                App.appContext().pref.setUserId(response.value?.userId)
                mDataManager.updateToken()
                mView?.showProgress(false)
                mView?.closeScreenAndContinue()
            }, { error: Throwable ->
                mView?.showProgress(false)
                if (error is ServerException) {
                    if(error.code == ERROR_403){
                        mView?.onRefreshTokenFailed()
                    } else {
                        mView?.showError(error.errorMessage)
                    }
                } else {
                    mView?.showError(error.message)
                }
            })


    }

    override fun vibrate(milliseconds: Long) {
        val vibrator = App.appContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) { // Vibrator availability checking
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        milliseconds,
                        100
                    )
                ) // New vibrate method for API Level 26 or higher
            } else {
                vibrator.vibrate(milliseconds) // Vibrate method for below API Level 26
            }
        }
    }

    override fun vibrateError() {
        val pattern = longArrayOf(0, 55, 55, 55)
        val vibrator = App.appContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) { // Vibrator availability checking
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val amplitudes = IntArray(pattern.size)
                for (i in 0 until pattern.size / 2) {
                    amplitudes[i * 2 + 1] = 170
                }
                vibrator.vibrate(
                    VibrationEffect.createWaveform(
                        pattern,
                        amplitudes,
                        -1
                    )
                ) // New vibrate method for API Level 26 or higher
            } else {
                vibrator.vibrate(pattern, -1)// Vibrate method for below API Level 26
            }
        }
    }
}