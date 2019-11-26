package com.app.belcobtm.ui.main.coins.visibility

import com.app.belcobtm.db.DbCryptoCoin
import com.app.belcobtm.db.DbCryptoCoinModel
import com.app.belcobtm.mvp.BaseMvpPresenterImpl
import io.realm.Realm


class VisibilityCoinsPresenter : BaseMvpPresenterImpl<VisibilityCoinsContract.View>(),
    VisibilityCoinsContract.Presenter {

    private val realm = Realm.getDefaultInstance()
    private val coinModel = DbCryptoCoinModel()
    override var coinsList: ArrayList<DbCryptoCoin> = coinModel.getAllCryptoCoin(realm)

    override fun onCoinVisibilityChanged(position: Int, visibility: Boolean) {
        coinModel.editCryptoCoin(realm, coinsList[position])
    }
}