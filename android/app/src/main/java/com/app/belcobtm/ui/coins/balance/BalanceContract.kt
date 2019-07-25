package com.app.belcobtm.ui.coins.balance

import com.app.belcobtm.api.model.response.GetCoinsResponse
import com.app.belcobtm.mvp.BaseMvpPresenter
import com.app.belcobtm.mvp.BaseMvpView


object BalanceContract {
    interface Presenter : BaseMvpPresenter<View> {
        val visibleCoins: ArrayList<GetCoinsResponse.CoinModel>
        val balance: Double
        fun requestCoins()
        fun checkPinEntered()
        fun checkCoinVisibility()
    }

    interface View : BaseMvpView {
        fun notifyData()
        fun onPinSaved()
        fun onPinNotSaved()
        fun onTokenNotSaved()

    }
}