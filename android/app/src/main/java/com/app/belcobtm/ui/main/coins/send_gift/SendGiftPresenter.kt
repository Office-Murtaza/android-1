package com.app.belcobtm.ui.main.coins.send_gift

import com.app.belcobtm.api.model.param.trx.Trx
import android.content.Context
import com.app.belcobtm.App
import com.app.belcobtm.R
import com.app.belcobtm.api.data_manager.WithdrawDataManager
import com.app.belcobtm.api.model.ServerException
import com.app.belcobtm.api.model.param.SendTransactionParam
import com.app.belcobtm.api.model.response.*
import com.app.belcobtm.db.DbCryptoCoin
import com.app.belcobtm.db.DbCryptoCoinModel
import com.app.belcobtm.mvp.BaseMvpDIPresenterImpl
import com.app.belcobtm.util.*
import com.giphy.sdk.core.models.Media
import com.google.gson.Gson
import io.realm.Realm
import wallet.core.jni.*


class SendGiftPresenter : BaseMvpDIPresenterImpl<SendGiftContract.View, WithdrawDataManager>(),
    SendGiftContract.Presenter {
    private var phoneEncoded: String? = null
    private var message: String? = null
    private var coinAmount: Double? = 0.0
    private var coinType: CoinType? = null
    private var fromAddress: String? = null
    override var phone: String?
        get() = _phone
        set(value) {
            _phone = value
        }
    override var gifMedia: Media?
        get() = _gifMedia
        set(value) {

            _gifMedia = value
        }

    private var _phone: String? = null
    private var _gifMedia: Media? = null

    override fun injectDependency() {
        presenterComponent.inject(this)
    }

    private val realm = Realm.getDefaultInstance()
    private val coinModel = DbCryptoCoinModel()
    val mUserId = App.appContext().pref.getUserId().toString()

    private var mTransactionHash :String? = null
    private var mTransactionHashJson :String? = null
    private var mCoinDbModel: DbCryptoCoin? = null

    override fun getCoinTransactionHash(
        context: Context,
        coin: CoinModel,
        phone: String,
        coinAmount: Double,
        message: String?
    ) {
        val seed = App.appContext().pref.getSeed()
        val hdWallet = HDWallet(seed, "")

        mCoinDbModel = coinModel.getCryptoCoin(realm, coin.coinId)

        this.coinType = if (mCoinDbModel != null) {
            CoinType.createFromValue(mCoinDbModel!!.coinTypeId)
        } else {
            mView?.showError(context.getString(R.string.wrong_crypto_coin_data))
            return
        }

        this.coinAmount = coinAmount
        this.message = message
        this.phone = phone

        this.phoneEncoded = "+" + phone.replace("+","")
        mDataManager.giftAddress(mUserId, mCoinDbModel!!.coinType, phoneEncoded)
            .flatMap { res ->
                this.fromAddress = res.value?.address
                getCoinTransactionHashObs(
                    hdWallet,
                    res.value?.address ?: "",
                    coinType,
                    coinAmount,
                    mCoinDbModel,mDataManager
                )

            }.flatMap { transactionHash ->


                if(CoinType.TRON == coinType){
                    mTransactionHashJson = transactionHash
                    mTransactionHash = null
                }else{
                    mTransactionHashJson = null
                    mTransactionHash = transactionHash
                }

               // Log.v("TRANSACTION_HEX", mTransactionHash)
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
                        3,
                        coinAmount,
                        phoneEncoded,
                        message,
                        gifMedia?.id,
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
                    if (error is ServerException && error.code != Const.ERROR_403) {
                        mView?.openSmsCodeDialog(error.errorMessage)
                    } else {
                        checkError(error)
                    }

                })
    }



}