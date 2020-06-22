package com.app.belcobtm.ui.main.coins.withdraw

import com.app.belcobtm.R
import com.app.belcobtm.api.data_manager.WithdrawDataManager
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.transaction.interactor.CreateWithdrawTransactionUseCase
import com.app.belcobtm.domain.transaction.interactor.WithdrawUseCase
import com.app.belcobtm.mvp.BaseMvpDIPresenterImpl
import org.koin.core.KoinComponent
import org.koin.core.inject

class WithdrawPresenter : BaseMvpDIPresenterImpl<WithdrawContract.View, WithdrawDataManager>(),
    WithdrawContract.Presenter, KoinComponent {
    private val createTransactionUseCase: CreateWithdrawTransactionUseCase by inject()
    private val withdrawUseCase: WithdrawUseCase by inject()
    private var mTransactionHash: String? = null
    private var coinCode: String? = null
    private var coinAmount: Double? = null

    override fun injectDependency() = presenterComponent.inject(this)

    override fun getCoinTransactionHash(
        coinId: String,
        toAddress: String,
        coinAmount: Double
    ) {
        this.coinCode = coinId
        this.coinAmount = coinAmount
        mView?.showProgress(true)
        createTransactionUseCase.invoke(
            CreateWithdrawTransactionUseCase.Params(coinId, coinAmount, toAddress),
            onSuccess = { hash ->
                mTransactionHash = hash
                mView?.showProgress(false)
                mView?.openSmsCodeDialog()
            },
            onError = {
                when (it) {
                    is Failure.TokenError -> mView?.onRefreshTokenFailed()
                    is Failure.MessageError -> mView?.showError(it.message)
                    is Failure.NetworkConnection -> mView?.showError(R.string.error_internet_unavailable)
                    else -> mView?.showError(R.string.error_something_went_wrong)
                }
            }
        )
    }

    override fun verifySmsCode(code: String) {
        val hash = mTransactionHash
        val fromCoinCode = coinCode
        val fromCoinAmount = coinAmount
        if (!hash.isNullOrBlank() && !fromCoinCode.isNullOrBlank() && fromCoinAmount != null) {
            mView?.showProgress(true)
            withdrawUseCase.invoke(
                WithdrawUseCase.Params(code, hash, fromCoinCode, fromCoinAmount),
                onSuccess = {
                    mView?.showProgress(false)
                    mView?.onTransactionDone()
                },
                onError = {
                    when (it) {
                        is Failure.TokenError -> mView?.onRefreshTokenFailed()
                        is Failure.MessageError -> mView?.openSmsCodeDialog(it.message)
                        is Failure.NetworkConnection -> mView?.showError(R.string.error_internet_unavailable)
                        else -> mView?.showError(R.string.error_something_went_wrong)
                    }
                }
            )
        }
    }
}