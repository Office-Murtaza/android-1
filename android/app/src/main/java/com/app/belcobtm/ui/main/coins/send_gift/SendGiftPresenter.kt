package com.app.belcobtm.ui.main.coins.send_gift

import android.content.Context
import com.app.belcobtm.R
import com.app.belcobtm.api.data_manager.WithdrawDataManager
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.transaction.interactor.CreateTransactionUseCase
import com.app.belcobtm.domain.transaction.interactor.SendGiftTransactionComplteUseCase
import com.app.belcobtm.domain.transaction.interactor.SendGiftTransactionCreateUseCase
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.mvp.BaseMvpDIPresenterImpl
import com.giphy.sdk.core.models.Media
import org.koin.core.KoinComponent
import org.koin.core.inject

class SendGiftPresenter : BaseMvpDIPresenterImpl<SendGiftContract.View, WithdrawDataManager>(),
    SendGiftContract.Presenter, KoinComponent {
    private val getGiftAddressUseCase: SendGiftTransactionCreateUseCase by inject()
    private val createTransactionUseCase: CreateTransactionUseCase by inject()
    private val sendGiftAddressUseCase: SendGiftTransactionComplteUseCase by inject()
    private var coinFromCode: String = ""
    private var transactionHash: String = ""
    private var fromAddress: String = ""
    private var phoneEncoded: String = ""
    private var message: String = ""
    private var coinAmount: Double = 0.0
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

    override fun injectDependency() = presenterComponent.inject(this)

    override fun createTransaction(
        context: Context,
        coinModel: CoinDataItem,
        phone: String,
        coinAmount: Double,
        message: String
    ) {
        this.coinFromCode = coinModel.code
        this.coinAmount = coinAmount
        this.message = message
        this.phone = phone
        this.phoneEncoded = "+" + phone.replace("+", "")

        mView?.showProgress(true)
        getGiftAddressUseCase.invoke(
            SendGiftTransactionCreateUseCase.Params(phoneEncoded, coinFromCode, 0.0),
            onSuccess = { giftAddress ->
                this.fromAddress = giftAddress
                createTransaction(coinFromCode, coinAmount)
            },
            onError = { errorResponse(it) }
        )
    }

    override fun completeTransaction(smsCode: String) {
        mView?.showProgress(true)
        sendGiftAddressUseCase.invoke(
            SendGiftTransactionComplteUseCase.Params(
                smsCode = smsCode,
                hash = transactionHash,
                coinFrom = coinFromCode,
                coinFromAmount = coinAmount,
                giftId = gifMedia?.id ?: "",
                phone = phoneEncoded,
                message = message
            ),
            onSuccess = {
                mView?.showProgress(false)
                mView?.onTransactionDone()
            },
            onError = { errorResponse(it) }

        )
    }

    private fun createTransaction(fromCoinCode: String, fromCoinAmount: Double) = createTransactionUseCase.invoke(
        CreateTransactionUseCase.Params(fromCoinCode, fromCoinAmount),
        onSuccess = { hash ->
            this.transactionHash = hash
            mView?.openSmsCodeDialog()
            mView?.showProgress(false)
        },
        onError = { errorResponse(it) }
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