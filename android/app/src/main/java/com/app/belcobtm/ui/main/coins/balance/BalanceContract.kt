package com.app.belcobtm.ui.main.coins.balance

import com.app.belcobtm.api.model.response.CoinModel
import com.app.belcobtm.mvp.BaseMvpPresenter
import com.app.belcobtm.mvp.BaseMvpView


object BalanceContract {
    interface Presenter : BaseMvpPresenter<View> {
        val coinsList: ArrayList<CoinModel>
        val balance: Double
        fun requestCoins()
    }

    interface View : BaseMvpView {
        fun notifyData()

    }
}