package com.app.belcobtm.mvp

import android.preference.PreferenceManager
import com.app.belcobtm.App
import com.app.belcobtm.api.data_manager.BaseDataManager
import com.app.belcobtm.api.model.ServerException
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.di.component.DaggerPresenterComponent
import com.app.belcobtm.di.component.PresenterComponent
import com.app.belcobtm.di.module.PresenterModule
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.presentation.core.Const
import com.app.belcobtm.presentation.core.extensions.CoinTypeExtension
import javax.inject.Inject

abstract class BaseMvpDIPresenterImpl<V : BaseMvpView, T : BaseDataManager> : BaseMvpPresenter<V> {
    protected var mView: V? = null
    protected val presenterComponent: PresenterComponent = DaggerPresenterComponent.builder()
        .presenterModule(PresenterModule())
        .build()

    //TODO need migrate to dependency koin after refactoring
    private val prefsHelper: SharedPreferencesHelper by lazy {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.appContext())
        SharedPreferencesHelper(sharedPreferences)
    }

    protected abstract fun injectDependency()

    @Inject
    protected lateinit var mDataManager: T

    override fun attachView(view: V) {
        injectDependency()
        mView = view
    }

    override fun detachView() {
        mView = null
    }

    protected fun <T : Throwable> onError(exception: T) {
        mView?.showError(exception.message)
    }

    protected fun checkError(error: Throwable) {
        println(error)
        mView?.showProgress(false)
        if (error is ServerException) {
            if (error.code == Const.ERROR_403) {
                mView?.onRefreshTokenFailed()
            } else {
                mView?.showError(error.errorMessage)
            }
        } else {
            mView?.showError(error.message)
        }
    }

    open fun validateAddress(
        coinId: String,
        walletAddress: String
    ): Boolean = CoinTypeExtension.getTypeByCode(
        if (LocalCoinType.CATM.name == coinId) LocalCoinType.ETH.name else coinId
    )?.validate(walletAddress) ?: false

    open fun getTransactionFee(coinName: String): Double = prefsHelper.coinsFee[coinName]?.txFee ?: 0.0
}