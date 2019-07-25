package com.app.belcobtm.ui.coins.visibility

import com.app.belcobtm.api.model.response.GetCoinsResponse
import com.app.belcobtm.db.DbCryptoCoin
import com.app.belcobtm.mvp.BaseMvpPresenter
import com.app.belcobtm.mvp.BaseMvpView


object VisibilityCoinsContract {
    interface Presenter : BaseMvpPresenter<View>, VisibilityCoinsAdapter.OnCoinVisibilityChangedListener {
        val coinsList: ArrayList<DbCryptoCoin>
    }

    interface View : BaseMvpView {
        fun notifyList()
    }
}