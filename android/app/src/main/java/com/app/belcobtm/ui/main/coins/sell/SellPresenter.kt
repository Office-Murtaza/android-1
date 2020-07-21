package com.app.belcobtm.ui.main.coins.sell

import com.app.belcobtm.R
import com.app.belcobtm.api.data_manager.WithdrawDataManager
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.tools.interactor.OldSendSmsToDeviceUseCase
import com.app.belcobtm.domain.transaction.interactor.SellGetLimitsUseCase
import com.app.belcobtm.domain.transaction.interactor.SellPreSubmitUseCase
import com.app.belcobtm.domain.transaction.interactor.SellUseCase
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.mvp.BaseMvpDIPresenterImpl
import org.koin.core.KoinComponent
import org.koin.core.inject

class SellPresenter : BaseMvpDIPresenterImpl<SellContract.View, WithdrawDataManager>(), SellContract.Presenter,
    KoinComponent {
    private val limitsUseCase: SellGetLimitsUseCase by inject()
    private val sendSmsUseCase: OldSendSmsToDeviceUseCase by inject()
    private val preSubmitUseCase: SellPreSubmitUseCase by inject()
    private val sellUseCase: SellUseCase by inject()

    private var cryptoResultAmount: Double = Double.MIN_VALUE
    private var addressDestination: String? = null

    private var balance: Double = Double.MIN_VALUE
    private var fiatAmount: Int? = null
    private var cryptoAmount: Double? = null
    private var isAnotherAddress: Boolean = false

    private var mCoin: CoinDataItem? = null

    override fun injectDependency() = presenterComponent.inject(this)

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

        sendSmsUseCase.invoke(Unit,
            onSuccess = { mView?.openSmsCodeDialog() },
            onError = { errorResponse(it) })
    }

    override fun verifySmsCode(smsCode: String) {
        mView?.showProgress(true)
        preSubmitUseCase.invoke(
            SellPreSubmitUseCase.Params(
                smsCode,
                mCoin?.code ?: "",
                cryptoAmount ?: 0.0,
                fiatAmount ?: 0
            ),
            onSuccess = { response ->
                addressDestination = response.address
                cryptoResultAmount = response.fromCoinAmount
                when {
                    response.address.isBlank() -> {
                        mView?.showProgress(false)
                        mView?.showErrorAndHideDialogs(R.string.error_transaction_cannot_be_created)
                    }
                    !isAnotherAddress && cryptoResultAmount < balance -> sellTransaction()
                    !isAnotherAddress && cryptoResultAmount >= balance -> errorResponse(Failure.MessageError("coin stock value has been changed"))
                    else -> {
                        mView?.showProgress(false)
                        mView?.showDoneScreenAnotherAddress(addressDestination, cryptoResultAmount)
                    }
                }
            },
            onError = {
                mView?.showProgress(false)
                when (it) {
                    is Failure.TokenError -> mView?.onRefreshTokenFailed()
                    is Failure.MessageError -> mView?.openSmsCodeDialog(it.message)
                    is Failure.NetworkConnection -> mView?.showErrorAndHideDialogs(R.string.error_internet_unavailable)
                    else -> mView?.showErrorAndHideDialogs(R.string.error_something_went_wrong)
                }
            }
        )
    }

    override fun bindData(mCoin: CoinDataItem?) {
        this.mCoin = mCoin
    }

    override fun getDetails() = limitsUseCase.invoke(
        Unit,
        onSuccess = { mView?.showLimits(it) },
        onError = { errorResponse(it) }
    )

    private fun sellTransaction() = sellUseCase.invoke(
        SellUseCase.Params(mCoin?.code ?: "", cryptoResultAmount),
        onSuccess = {
            mView?.showProgress(false)
            mView?.showDoneScreen()
        },
        onError = {
            mView?.showProgress(false)
            errorResponse(it)
        }
    )

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