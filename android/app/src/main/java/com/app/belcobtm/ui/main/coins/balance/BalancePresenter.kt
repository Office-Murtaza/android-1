package com.app.belcobtm.ui.main.coins.balance

import android.preference.PreferenceManager
import com.app.belcobtm.App
import com.app.belcobtm.api.data_manager.CoinsDataManager
import com.app.belcobtm.api.model.response.CoinModel
import com.app.belcobtm.api.model.response.GetCoinsFeeOldResponse
import com.app.belcobtm.api.model.response.GetCoinsResponse
import com.app.belcobtm.data.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.db.DbCryptoCoinModel
import com.app.belcobtm.mvp.BaseMvpDIPresenterImpl
import com.app.belcobtm.presentation.core.Optional
import io.realm.Realm


class BalancePresenter : BaseMvpDIPresenterImpl<BalanceContract.View, CoinsDataManager>(),
    BalanceContract.Presenter {

    override val coinsList: ArrayList<CoinModel> = arrayListOf()
    override var balance: Double = 0.0

    //TODO need migrate to dependency koin after refactoring
    private val prefsHelper: SharedPreferencesHelper by lazy {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.appContext())
        SharedPreferencesHelper(sharedPreferences)
    }
    private val realm = Realm.getDefaultInstance()
    private val coinModel = DbCryptoCoinModel()


    override fun injectDependency() {
        presenterComponent.inject(this)
    }

    override fun requestCoins() {
        if (prefsHelper.apiSeed.isNotEmpty()) {
            mView?.showProgress(true)
            val userId = prefsHelper.userId.toString()
            val visibleCoinsNames = getVisibleCoinsNames()
            mDataManager.getCoins(userId, visibleCoinsNames)
                .subscribe({ response: Optional<GetCoinsResponse> ->
                    mView?.showProgress(false)
                    balance = response.value!!.totalBalance.uSD
                    coinsList.clear()
                    coinsList.addAll(response.value!!.coins)
                    mView?.notifyData()
                }, { error: Throwable ->
                    checkError(error)
                })

            mView?.showProgress(true)
        }
//
//        mDataManager.getCoinsFee(userId).subscribe({ resp: Optional<GetCoinsFeeOldResponse> ->
//            mView?.showProgress(false)
//            prefsHelper.coinsFee = resp.value?.fees ?: emptyList()
//        }, { error: Throwable ->
//            checkError(error)
//        })
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