package com.app.belcobtm.ui.main.coins.balance

import android.preference.PreferenceManager
import com.app.belcobtm.App
import com.app.belcobtm.api.data_manager.CoinsDataManager
import com.app.belcobtm.api.model.response.CoinModel
import com.app.belcobtm.api.model.response.GetCoinsResponse
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.domain.wallet.interactor.GetCoinListUseCase
import com.app.belcobtm.mvp.BaseMvpDIPresenterImpl
import com.app.belcobtm.presentation.core.Optional
import org.koin.core.KoinComponent
import org.koin.core.inject


class BalancePresenter : BaseMvpDIPresenterImpl<BalanceContract.View, CoinsDataManager>(),
    BalanceContract.Presenter, KoinComponent {
    private val coinListUseCase: GetCoinListUseCase by inject()

    //TODO need migrate to dependency koin after refactoring
    private val prefsHelper: SharedPreferencesHelper by lazy {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.appContext())
        SharedPreferencesHelper(sharedPreferences)
    }

    override val coinsList: ArrayList<CoinModel> = arrayListOf()
    override var balance: Double = 0.0

    override fun injectDependency() {
        presenterComponent.inject(this)
    }

    override fun requestCoins() {
        if (prefsHelper.apiSeed.isNotEmpty()) {
            mView?.showProgress(true)
            coinListUseCase.invoke { coinList ->
                val userId = prefsHelper.userId.toString()
                val visibleCoinsNames = coinList.filter { it.isEnabled }.map { it.type.name }
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
            }
        }
    }
}