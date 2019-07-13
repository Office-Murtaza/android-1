package com.app.belcobtm.ui.coins.balance

import com.app.belcobtm.App
import com.app.belcobtm.api.data_manager.CoinsDataManager
import com.app.belcobtm.api.model.ServerException
import com.app.belcobtm.api.model.response.GetCoinsResponse
import com.app.belcobtm.mvp.BaseMvpPresenterImpl
import com.app.belcobtm.util.Optional
import com.app.belcobtm.util.pref


class BalancePresenter : BaseMvpPresenterImpl<BalanceContract.View, CoinsDataManager>(),
    BalanceContract.Presenter {

    override val coinsList: ArrayList<GetCoinsResponse.CoinModel> = arrayListOf()
    override var balance: Double = 0.0

    override fun injectDependency() {
        presenterComponent.inject(this)
    }

    override fun checkPinEntered() {
        val pin = App.appContext().pref.getPin()
        val token = App.appContext().pref.getSessionApiToken()
        when {
            token == null -> mView?.onTokenNotSaved()
            pin != null -> mView?.onPinSaved()
            else -> mView?.onPinNotSaved()
        }
    }

    override fun requestCoins() {
        val userId = App.appContext().pref.getUserId().toString()
        mDataManager.getCoins(userId)
            .subscribe({ response: Optional<GetCoinsResponse> ->
                mView?.showProgress(false)
                balance = response.value!!.totalBalance.uSD
                coinsList.clear()
                coinsList.addAll(response.value!!.coins)
                mView?.notifyData()
            }
                , { error: Throwable ->
                    checkError(error)
                })
    }
}