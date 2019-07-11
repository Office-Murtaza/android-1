package com.app.belcobtm.ui.coins.balance

import com.app.belcobtm.api.model.response.GetCoinsResponse
import com.app.belcobtm.mvp.BaseMvpPresenter
import com.app.belcobtm.mvp.BaseMvpView


object BalanceContract {
    interface Presenter : BaseMvpPresenter<View> {
        val coinsList: ArrayList<GetCoinsResponse.CoinModel>
        val balance: Double
        fun requestCoins()
    }

    interface View : BaseMvpView {
        fun showProgress(show: Boolean)
        fun notifyData()
    }
}