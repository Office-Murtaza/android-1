package com.app.belcobtm.ui.main.coins.details

import android.preference.PreferenceManager
import com.app.belcobtm.App
import com.app.belcobtm.api.data_manager.WithdrawDataManager
import com.app.belcobtm.api.model.response.CoinModel
import com.app.belcobtm.api.model.response.TransactionModel
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.mvp.BaseMvpDIPresenterImpl


class DetailsPresenter : BaseMvpDIPresenterImpl<DetailsContract.View, WithdrawDataManager>(),
    DetailsContract.Presenter {
    private lateinit var transaction: TransactionModel
    private var coin: CoinModel? = null
    //TODO need migrate to dependency koin after refactoring
    private val prefsHelper: SharedPreferencesHelper by lazy {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.appContext())
        SharedPreferencesHelper(sharedPreferences)
    }

    override fun injectDependency() {
        presenterComponent.inject(this)
    }

    override fun bindData(coin: CoinModel?, transaction: TransactionModel) {
        this.coin = coin
        this.transaction = transaction

    }

    override fun getDetails() {

        mDataManager.getTransactionDetails(
            prefsHelper.userId.toString(),
            coin?.coinId ?: "",
            transaction.txid,
            transaction.txDbId
        ).subscribe({ response ->
            if (response.value?.txId != null || response.value?.txDbId != null) {
                mView?.showTransactionDetails(response.value)
            }
        }, { error ->
            checkError(error)
        })
    }
}