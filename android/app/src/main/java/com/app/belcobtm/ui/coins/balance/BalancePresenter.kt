package com.app.belcobtm.ui.coins.balance

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

    private val coinsList: ArrayList<GetCoinsResponse.CoinModel> = arrayListOf()
    override var balance: Double = 0.0

    private val realm = Realm.getDefaultInstance()
    private val coinModel = DbCryptoCoinModel()
    override var visibleCoins: ArrayList<GetCoinsResponse.CoinModel> = arrayListOf()


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
        if (coinsList.isEmpty()) {
            mView?.showProgress(true)
        }
        val userId = App.appContext().pref.getUserId().toString()
        mDataManager.getCoins(userId).subscribe(
            { response: Optional<GetCoinsResponse> ->
                mView?.showProgress(false)
                balance = response.value!!.totalBalance.uSD
                coinsList.clear()
                coinsList.addAll(response.value!!.coins)
                checkCoinVisibility()
            }
            , { error: Throwable ->
                checkError(error)
            })
    }

    override fun checkCoinVisibility() {
        val dbVisibleCoins = coinModel.getAllVisibleCryptoCoin(realm)
        visibleCoins.clear()

        dbVisibleCoins.forEach { dbVisibleCoin ->
            coinsList.forEach { apiCoin ->
                if (apiCoin.coinId == dbVisibleCoin.coinType) {
                    visibleCoins.add(apiCoin)
                    return@forEach
                }
            }
        }

        mView?.notifyData()
    }

}