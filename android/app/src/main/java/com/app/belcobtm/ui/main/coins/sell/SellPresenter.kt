package com.app.belcobtm.ui.main.coins.sell

import com.app.belcobtm.R
import com.app.belcobtm.api.data_manager.WithdrawDataManager
import com.app.belcobtm.api.model.response.CoinModel
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.interactor.SellGetLimitsUseCase
import com.app.belcobtm.domain.wallet.interactor.SellPreSubmitUseCase
import com.app.belcobtm.domain.wallet.interactor.SellUseCase
import com.app.belcobtm.domain.wallet.interactor.SendSmsToDeviceUseCase
import com.app.belcobtm.mvp.BaseMvpDIPresenterImpl
import org.koin.core.KoinComponent
import org.koin.core.inject

class SellPresenter : BaseMvpDIPresenterImpl<SellContract.View, WithdrawDataManager>(), SellContract.Presenter,
    KoinComponent {
    private val limitsUseCase: SellGetLimitsUseCase by inject()
    private val sendSmsUseCase: SendSmsToDeviceUseCase by inject()
    private val preSubmitUseCase: SellPreSubmitUseCase by inject()
    private val sellUseCase: SellUseCase by inject()

    private var cryptoResultAmount: Double = Double.MIN_VALUE
    private var addressDestination: String? = null

    private var balance: Double = Double.MIN_VALUE
    private var fiatAmount: Int? = null
    private var cryptoAmount: Double? = null
    private var isAnotherAddress: Boolean = false

    private var mCoin: CoinModel? = null

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

        sendSmsUseCase.invoke(Unit) { either ->
            either.either(
                { errorResponse(it) },
                { mView?.openSmsCodeDialog() }
            )
        }
    }

    override fun verifySmsCode(smsCode: String) {
        mView?.showProgress(true)
        preSubmitUseCase.invoke(
            SellPreSubmitUseCase.Params(
                smsCode,
                mCoin?.coinId ?: "",
                cryptoAmount ?: 0.0,
                fiatAmount ?: 0
            )
        ) { either ->
            either.either(
                {
                    mView?.showProgress(false)
                    when (it) {
                        is Failure.TokenError -> mView?.onRefreshTokenFailed()
                        is Failure.MessageError -> mView?.openSmsCodeDialog(it.message)
                        is Failure.NetworkConnection -> mView?.showErrorAndHideDialogs(R.string.error_internet_unavailable)
                        else -> mView?.showErrorAndHideDialogs(R.string.error_something_went_wrong)
                    }
                },
                { response ->
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
                }
            )
        }
    }

    override fun bindData(mCoin: CoinModel?) {
        this.mCoin = mCoin
    }

    override fun getDetails() = limitsUseCase.invoke(SellGetLimitsUseCase.Params(mCoin?.coinId ?: "")) { either ->
        either.either(
            { errorResponse(it) },
            { mView?.showLimits(it) }
        )
    }

    private fun sellTransaction() =
        sellUseCase.invoke(SellUseCase.Params(mCoin?.coinId ?: "", cryptoResultAmount)) { either ->
            either.either(
                {
                    mView?.showProgress(false)
                    errorResponse(it)
                },
                {
                    mView?.showProgress(false)
                    mView?.showDoneScreen()
                }
            )
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