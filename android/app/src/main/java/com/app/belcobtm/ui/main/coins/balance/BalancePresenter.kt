package com.app.belcobtm.ui.main.coins.balance

import com.app.belcobtm.App
import com.app.belcobtm.api.data_manager.CoinsDataManager
import com.app.belcobtm.api.model.response.GetCoinsResponse
import com.app.belcobtm.db.DbCryptoCoinModel
import com.app.belcobtm.mvp.BaseMvpDIPresenterImpl
import com.app.belcobtm.util.Optional
import com.app.belcobtm.util.pref
import io.realm.Realm


class BalancePresenter : BaseMvpDIPresenterImpl<BalanceContract.View, CoinsDataManager>(),
    BalanceContract.Presenter {

    override val coinsList: ArrayList<GetCoinsResponse.CoinModel> = arrayListOf()
    override var balance: Double = 0.0

    private val realm = Realm.getDefaultInstance()
    private val coinModel = DbCryptoCoinModel()


    override fun injectDependency() {
        presenterComponent.inject(this)
    }

    override fun requestCoins() {
        mView?.showProgress(true)
        val userId = App.appContext().pref.getUserId().toString()
        val visibleCoinsNames = getVisibleCoinsNames()
        mDataManager.getCoins(userId, visibleCoinsNames).subscribe(
            { response: Optional<GetCoinsResponse> ->
                mView?.showProgress(false)
                balance = response.value!!.totalBalance.uSD
                coinsList.clear()
                coinsList.addAll(response.value!!.coins)
                mView?.notifyData()
//                checkCoinVisibility()
            }
            , { error: Throwable ->
                checkError(error)
            })
    }

    private fun getVisibleCoinsNames(): ArrayList<String> {
        val dbVisibleCoins = coinModel.getAllVisibleCryptoCoin(realm)
        var names: ArrayList<String> = arrayListOf()
        dbVisibleCoins.forEach {
            if (it.visible)
                names.add(it.coinType)
        }

        return names
    }

}