package com.app.belcobtm.ui.main.coins.withdraw

import android.content.Context
import com.app.belcobtm.App
import com.app.belcobtm.R
import com.app.belcobtm.api.data_manager.WithdrawDataManager
import com.app.belcobtm.api.model.ServerException
import com.app.belcobtm.api.model.param.SendTransactionParam
import com.app.belcobtm.api.model.param.trx.Trx
import com.app.belcobtm.db.DbCryptoCoin
import com.app.belcobtm.db.DbCryptoCoinModel
import com.app.belcobtm.mvp.BaseMvpDIPresenterImpl
import com.app.belcobtm.util.Const
import com.app.belcobtm.util.pref
import com.google.gson.Gson
import io.realm.Realm
import wallet.core.jni.CoinType
import wallet.core.jni.HDWallet


class WithdrawPresenter : BaseMvpDIPresenterImpl<WithdrawContract.View, WithdrawDataManager>(),
    WithdrawContract.Presenter {
    override fun injectDependency() {
        presenterComponent.inject(this)
    }

    private var coinAmount: Double? = null
    private val realm = Realm.getDefaultInstance()
    private val coinModel = DbCryptoCoinModel()
    val mUserId = App.appContext().pref.getUserId().toString()

    private var mTransactionHash: String? = null
    private var mTransactionHashJson: String? = null
    private var mCoinDbModel: DbCryptoCoin? = null

    override fun getCoinTransactionHash(
        context: Context,
        coinId: String,
        toAddress: String,
        coinAmount: Double
    ) {
        this.coinAmount = coinAmount
        val seed = App.appContext().pref.getSeed()
        val hdWallet = HDWallet(seed, "")

        mCoinDbModel = coinModel.getCryptoCoin(realm, coinId)

        val coinType = if (mCoinDbModel != null) {
            CoinType.createFromValue(mCoinDbModel!!.coinTypeId)
        } else {
            mView?.showError(context.getString(R.string.wrong_crypto_coin_data))
            return
        }

        getCoinTransactionHashObs(
            hdWallet,
            toAddress,
            coinType,
            coinAmount,
            mCoinDbModel,
            mDataManager
        )
            .flatMap { transactionHash ->
                // mTransactionHash = transactionHash

                if (CoinType.TRON == coinType) {
                    mTransactionHashJson = transactionHash
                    mTransactionHash = null
                } else {
                    mTransactionHashJson = null
                    mTransactionHash = transactionHash
                }

                mDataManager.requestSmsCode(mUserId)
            }
            .subscribe({ response ->
                if (response.value!!.sent) {
                    mView?.openSmsCodeDialog()
                }
            }, { error -> checkError(error) })
    }


    override fun verifySmsCode(code: String) {
        mView?.showProgress(true)

        mDataManager.verifySmsCode(mUserId, code)
            .flatMap { res ->

                mDataManager.submitTx(
                    mUserId,
                    mCoinDbModel!!.coinType,
                    SendTransactionParam(
                        2,
                        coinAmount,
                        null,
                        null,
                        null,
                        mTransactionHash?.substring(2),
                        Gson().fromJson<Trx>(mTransactionHashJson, Trx::class.java)
                    )
                )


            }
            .subscribe(
                {
                    mView?.showProgress(false)
                    mView?.onTransactionDone()
                }
                ,
                { error: Throwable ->
                    mView?.showProgress(false)
                    //todo add handling verifySms error and sendHash error
                    if (error is ServerException && error.code != Const.ERROR_403) {
                        mView?.openSmsCodeDialog(error.errorMessage)
                    } else {
                        checkError(error)
                    }

                })
    }
}