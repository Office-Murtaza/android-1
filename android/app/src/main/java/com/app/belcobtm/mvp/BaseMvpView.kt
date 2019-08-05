package com.app.belcobtm.mvp

import android.content.Context
import androidx.annotation.StringRes


interface BaseMvpView {

    fun getContext(): Context

    fun showError(error: String?)

    fun showError(@StringRes stringResId: Int)

    fun showLongError(error: String?)

    fun showLongError(@StringRes stringResId: Int)

    fun showMessage(@StringRes srtResId: Int)

    fun showMessage(message: String?)

    fun showProgress(show: Boolean)

    fun onRefreshTokenFailed()

}