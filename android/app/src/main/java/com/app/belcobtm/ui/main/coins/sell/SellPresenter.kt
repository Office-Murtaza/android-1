package com.app.belcobtm.ui.main.coins.sell

import android.preference.PreferenceManager
import com.app.belcobtm.App
import com.app.belcobtm.R
import com.app.belcobtm.api.data_manager.WithdrawDataManager
import com.app.belcobtm.api.model.ServerException
import com.app.belcobtm.api.model.param.PreTransactionParam
import com.app.belcobtm.api.model.param.SendTransactionParam
import com.app.belcobtm.api.model.param.trx.Trx
import com.app.belcobtm.api.model.response.CoinModel
import com.app.belcobtm.api.model.response.LimitsResponse
import com.app.belcobtm.data.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.db.DbCryptoCoin
import com.app.belcobtm.db.DbCryptoCoinModel
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.interactor.CreateTransactionUseCase
import com.app.belcobtm.domain.wallet.interactor.SendSmsToDeviceUseCase
import com.app.belcobtm.mvp.BaseMvpDIPresenterImpl
import com.app.belcobtm.presentation.core.Const
import com.google.gson.Gson
import io.reactivex.Observable
import io.realm.Realm
import org.koin.core.KoinComponent
import org.koin.core.inject
import wallet.core.jni.CoinType
import wallet.core.jni.HDWallet


class SellPresenter : BaseMvpDIPresenterImpl<SellContract.View, WithdrawDataManager>(), SellContract.Presenter,
    KoinComponent {
    private val sendSmsUseCase: SendSmsToDeviceUseCase by inject()

    //TODO need migrate to dependency koin after refactoring
    private val prefsHelper: SharedPreferencesHelper by lazy {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.appContext())
        SharedPreferencesHelper(sharedPreferences)
    }

    private var mTransactionHash: String? = null
    private var mTransactionHashJson: String? = null
    private var cryptoResultAmount: Double = Double.MIN_VALUE
    private var addressDestination: String? = null
    var isErrorOnSms = true

    private var balance: Double = Double.MIN_VALUE
    private var fiatAmount: Int? = null
    private var cryptoAmount: Double? = null
    private var isAnotherAddress: Boolean = false

    var limits: LimitsResponse? = null
    private var mCoin: CoinModel? = null
    private var message: String? = null
    private var coinAmount: Double? = 0.0
    private var coinType: CoinType? = null
    private var fromAddress: String? = null

    override fun injectDependency() = presenterComponent.inject(this)

    private val realm = Realm.getDefaultInstance()
    private val coinModel = DbCryptoCoinModel()
    val mUserId = prefsHelper.userId.toString()
    private var mCoinDbModel: DbCryptoCoin? = null

    override fun preSubmit(
        fiatAmount: Int,
        cryptoAmount: Double,
        balance: Double,
        checked: Boolean
    ) {
        this.balance = balance
        this.fiatAmount = fiatAmount
        this.cryptoAmount = cryptoAmount
        this.isAnotherAddress = checked

        sendSmsUseCase.invoke(Unit) { either ->
            either.either(
                { errorResponse(it) },
                { mView?.openSmsCodeDialog() }
            )
        }
    }

    override fun verifySmsCode(code: String) {
        mView?.showProgress(true)

        isErrorOnSms = true
        mDataManager.verifySmsCode(mUserId, code)
            .flatMap { res ->
                isErrorOnSms = false
                mDataManager.preSubmitTx(
                    mUserId, mCoin?.coinId ?: "",
                    PreTransactionParam(cryptoAmount, fiatAmount, "USD")
                )
            }
            .flatMap { res ->
                mView?.showProgress(false)
                this.addressDestination = res?.value?.address

                if (addressDestination.isNullOrEmpty()) {
                    Observable.error(Throwable("the transaction can not be created"))
                } else {
                    this.cryptoResultAmount = res?.value?.cryptoAmount ?: Double.MIN_VALUE

                    if (isAnotherAddress) {
                        Observable.just("")
                    } else {
                        if (cryptoResultAmount >= balance) {
                            Observable.error(Throwable("coin stock value has been changed"))
                        } else {
                            val hdWallet = HDWallet(prefsHelper.apiSeed, "")
                            this.fromAddress = res.value?.address

                            val coinType = if (mCoinDbModel != null) {
                                CoinType.createFromValue(mCoinDbModel!!.coinTypeId)
                            } else {
                                null
                            }
                            Observable.defer {
                                getCoinTransactionHashObs(
                                    hdWallet,
                                    res.value?.address ?: "",
                                    coinType,
                                    cryptoResultAmount ?: Double.MIN_VALUE,
                                    mCoinDbModel, mDataManager
                                )
                            }
                        }
                    }
                }
            }
            .flatMap { transactionHash ->
                if (CoinType.TRON == coinType) {
                    mTransactionHashJson = transactionHash
                    mTransactionHash = null
                } else {
                    mTransactionHashJson = null
                    mTransactionHash = transactionHash
                }

                if (transactionHash.isNullOrEmpty()) {
                    Observable.just("")
                } else {
                    mTransactionHash = if (mTransactionHashJson != null) {
                        Gson().toJson(Gson().fromJson<Trx>(mTransactionHashJson, Trx::class.java))
                    } else {
                        mTransactionHash?.substring(2)
                    }

                    mDataManager.submitTx(
                        mUserId,
                        mCoinDbModel!!.coinType,
                        SendTransactionParam(
                            6,
                            cryptoResultAmount,
                            null,
                            null,
                            null,
                            mTransactionHash,
                            null
                        )
                    ).subscribe(
                        {

                            mView?.showProgress(false)
                            mView?.onTransactionDone(
                                isAnotherAddress,
                                addressDestination,
                                cryptoResultAmount
                            )
                        }
                        ,
                        { error: Throwable ->
                            mView?.showProgress(false)
                            if (error is ServerException && error.code != Const.ERROR_403) {
                                mView?.showErrorAndHideDialogs(error.errorMessage)

                            } else {
                                checkError(error)
                            }

                        })

                    Observable.just("")
                }
            }
            .subscribe(
                {
                    mView?.showProgress(false)
                    mView?.onTransactionDone(
                        isAnotherAddress,
                        addressDestination,
                        cryptoResultAmount
                    )
                },
                { error: Throwable ->
                    mView?.showProgress(false)
                    if (error is ServerException && error.code != Const.ERROR_403) {

                        if (isErrorOnSms)
                            mView?.openSmsCodeDialog(error.errorMessage)
                        else
                            mView?.showErrorAndHideDialogs(error.errorMessage)

                    } else {
                        checkError(error)
                    }
                }
            )
    }


    override fun bindData(mCoin: CoinModel?) {
        this.mCoin = mCoin
        mCoinDbModel = coinModel.getCryptoCoin(realm, mCoin?.coinId ?: "")
    }

    override fun getDetails() {
        mDataManager.getLimits(mUserId, mCoinDbModel?.coinType).subscribe({ response ->
            if (response.value != null) {
                this.limits = response.value
                mView?.showLimits(response.value)
            }
        }, { error -> checkError(error) })
    }


    override fun validateAddress(coinId: String, walletAddress: String): Boolean {
        return true
    }

    private fun errorResponse(throwable: Throwable) {
        mView?.showProgress(false)
        when (throwable) {
            is Failure.TokenError -> mView?.onRefreshTokenFailed()
            is Failure.MessageError -> mView?.showError(throwable.message)
            is Failure.NetworkConnection -> mView?.showError(R.string.error_internet_unavailable)
            else -> mView?.showError(R.string.error_something_went_wrong)
        }
    }
}