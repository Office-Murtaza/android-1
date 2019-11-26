package com.app.belcobtm.ui.main.coins.details

import com.app.belcobtm.App
import com.app.belcobtm.api.data_manager.WithdrawDataManager
import com.app.belcobtm.api.model.response.CoinModel
import com.app.belcobtm.api.model.response.TransactionModel
import com.app.belcobtm.db.DbCryptoCoin
import com.app.belcobtm.db.DbCryptoCoinModel
import com.app.belcobtm.mvp.BaseMvpDIPresenterImpl
import com.app.belcobtm.util.pref
import io.realm.Realm
import wallet.core.jni.CoinType


class DetailsPresenter : BaseMvpDIPresenterImpl<DetailsContract.View, WithdrawDataManager>(),
    DetailsContract.Presenter {
    override fun bindData(coin: CoinModel?, transaction: TransactionModel) {
        this.coin = coin
        this.transaction = transaction

        mCoinDbModel = coinModel.getCryptoCoin(realm, coin?.coinId ?: "")

    }

    override fun getDetails() {

        mDataManager.getTransactionDetails(
            mUserId, mCoinDbModel?.coinType,
            transaction.txid
        ).subscribe({ response ->
            if (response.value?.txId != null) {
                mView?.showTransactionDetails(response.value)
            }
        }, { error -> checkError(error) })
    }

    private lateinit var transaction: TransactionModel
    private var coin: CoinModel? = null
    private var message: String? = null
    private var coinAmount: Double? = 0.0
    private var coinType: CoinType? = null
    private var fromAddress: String? = null

    override fun injectDependency() {
        presenterComponent.inject(this)
    }

    private val realm = Realm.getDefaultInstance()
    private val coinModel = DbCryptoCoinModel()
    val mUserId = App.appContext().pref.getUserId().toString()
    private var mCoinDbModel: DbCryptoCoin? = null


}