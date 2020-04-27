package com.app.belcobtm.ui.main.coins.withdraw

import com.app.belcobtm.R
import com.app.belcobtm.api.data_manager.WithdrawDataManager
import com.app.belcobtm.db.DbCryptoCoinModel
import com.app.belcobtm.db.mapToDataItem
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.interactor.CreateTransactionUseCase
import com.app.belcobtm.domain.wallet.interactor.WithdrawUseCase
import com.app.belcobtm.mvp.BaseMvpDIPresenterImpl
import io.realm.Realm
import org.koin.core.KoinComponent
import org.koin.core.inject

class WithdrawPresenter : BaseMvpDIPresenterImpl<WithdrawContract.View, WithdrawDataManager>(),
    WithdrawContract.Presenter, KoinComponent {
    private val createTransactionUseCase: CreateTransactionUseCase by inject()
    private val withdrawUseCase: WithdrawUseCase by inject()
    private var mTransactionHash: String? = null
    private var coinCode: String? = null
    private var coinAmount: Double? = null

    private val realm: Realm = Realm.getDefaultInstance()
    private val dbCryptoCoinModel: DbCryptoCoinModel = DbCryptoCoinModel()

    override fun injectDependency() = presenterComponent.inject(this)

    override fun getCoinTransactionHash(
        coinId: String,
        toAddress: String,
        coinAmount: Double
    ) {
        dbCryptoCoinModel.getCryptoCoin(realm, coinId)?.let { fromCoinDb ->
            this.coinCode = coinId
            this.coinAmount = coinAmount
            val coinDataItem = fromCoinDb.mapToDataItem()
            mView?.showProgress(true)
            createTransactionUseCase.invoke(CreateTransactionUseCase.Params(coinDataItem, coinAmount)) { either ->
                either.either({
                    when (it) {
                        is Failure.TokenError -> mView?.onRefreshTokenFailed()
                        is Failure.MessageError -> mView?.showError(it.message)
                        is Failure.NetworkConnection -> mView?.showError(R.string.error_internet_unavailable)
                        else -> mView?.showError(R.string.error_something_went_wrong)
                    }
                }, { hash ->
                    mTransactionHash = hash
                    mView?.showProgress(false)
                    mView?.openSmsCodeDialog()
                })
            }
        } ?: mView?.showError(R.string.error_please_try_again)
    }

    override fun verifySmsCode(code: String) {
        val hash = mTransactionHash
        val fromCoinCode = coinCode
        val fromCoinAmount = coinAmount
        if (!hash.isNullOrBlank() && !fromCoinCode.isNullOrBlank() && fromCoinAmount != null) {
            mView?.showProgress(true)
            withdrawUseCase.invoke(WithdrawUseCase.Params(code, hash, fromCoinCode, fromCoinAmount)) { either ->
                either.either({
                    when (it) {
                        is Failure.TokenError -> mView?.onRefreshTokenFailed()
                        is Failure.MessageError -> mView?.openSmsCodeDialog(it.message)
                        is Failure.NetworkConnection -> mView?.showError(R.string.error_internet_unavailable)
                        else -> mView?.showError(R.string.error_something_went_wrong)
                    }
                }, {
                    mView?.showProgress(false)
                    mView?.onTransactionDone()
                })
            }
        }
    }
}