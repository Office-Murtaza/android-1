package com.app.belcobtm.ui.main.coins.withdraw

import android.content.Context
import android.preference.PreferenceManager
import com.app.belcobtm.App
import com.app.belcobtm.R
import com.app.belcobtm.api.data_manager.WithdrawDataManager
import com.app.belcobtm.api.model.ServerException
import com.app.belcobtm.api.model.param.SendTransactionParam
import com.app.belcobtm.api.model.param.trx.Trx
import com.app.belcobtm.data.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.db.DbCryptoCoin
import com.app.belcobtm.db.DbCryptoCoinModel
import com.app.belcobtm.mvp.BaseMvpDIPresenterImpl
import com.app.belcobtm.presentation.core.Const
import com.google.gson.Gson
import io.realm.Realm
import wallet.core.jni.CoinType
import wallet.core.jni.HDWallet


class WithdrawPresenter : BaseMvpDIPresenterImpl<WithdrawContract.View, WithdrawDataManager>(),
    WithdrawContract.Presenter {
    override fun injectDependency() {
        presenterComponent.inject(this)
    }
    //TODO need migrate to dependency koin after refactoring
    private val prefsHelper: SharedPreferencesHelper by lazy {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.appContext())
        SharedPreferencesHelper(sharedPreferences)
    }

    private var coinAmount: Double? = null
    private val realm = Realm.getDefaultInstance()
    private val coinModel = DbCryptoCoinModel()
    val mUserId = prefsHelper.userId.toString()

    private var mTransactionHash: String? = null
    private var mTransactionHashJson: String? = null
    private var mCoinDbModel: DbCryptoCoin? = null

    init {
        toString()
    }

    override fun getCoinTransactionHash(
        context: Context,
        coinId: String,
        toAddress: String,
        coinAmount: Double
    ) {
        this.coinAmount = coinAmount
        val hdWallet = HDWallet(prefsHelper.apiSeed, "")

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

                if(mTransactionHashJson!=null )
                {
                    mTransactionHash = Gson().toJson(Gson().fromJson<Trx>(mTransactionHashJson, Trx::class.java))
                }else{
                    mTransactionHash =  mTransactionHash?.substring(2)
                }

                mDataManager.submitTx(
                    mUserId,
                    mCoinDbModel!!.coinType,
                    SendTransactionParam(
                        2,
                        coinAmount,
                        null,
                        null,
                        null,
                        mTransactionHash,
                        null
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
                    if (error is ServerException && error.code != Const.ERROR_403) {
                        mView?.openSmsCodeDialog(error.errorMessage)
                    } else {
                        checkError(error)
                    }

                })
    }
}