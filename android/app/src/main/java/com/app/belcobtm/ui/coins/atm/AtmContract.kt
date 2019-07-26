package com.app.belcobtm.ui.coins.atm

import com.app.belcobtm.api.model.response.AtmResponse
import com.app.belcobtm.api.model.response.GetCoinsResponse
import com.app.belcobtm.mvp.BaseMvpPresenter
import com.app.belcobtm.mvp.BaseMvpView


object AtmContract {
    interface Presenter : BaseMvpPresenter<View> {
        val atmAddressList: ArrayList<AtmResponse.AtmAddress>
        fun requestAtmAddressList()
    }

    interface View : BaseMvpView {
        fun notifyAtmAddressList()
    }
}