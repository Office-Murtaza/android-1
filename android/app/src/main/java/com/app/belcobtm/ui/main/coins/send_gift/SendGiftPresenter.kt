package com.app.belcobtm.ui.main.coins.send_gift

import android.content.Context
import com.app.belcobtm.R
import com.app.belcobtm.api.data_manager.WithdrawDataManager
import com.app.belcobtm.api.model.response.CoinModel
import com.app.belcobtm.db.DbCryptoCoinModel
import com.app.belcobtm.db.mapToDataItem
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.interactor.CreateTransactionUseCase
import com.app.belcobtm.domain.wallet.interactor.GetGiftAddressUseCase
import com.app.belcobtm.domain.wallet.interactor.SendGiftUseCase
import com.app.belcobtm.mvp.BaseMvpDIPresenterImpl
import com.giphy.sdk.core.models.Media
import io.realm.Realm
import org.koin.core.KoinComponent
import org.koin.core.inject

class SendGiftPresenter : BaseMvpDIPresenterImpl<SendGiftContract.View, WithdrawDataManager>(),
    SendGiftContract.Presenter, KoinComponent {
    private val getGiftAddressUseCase: GetGiftAddressUseCase by inject()
    private val createTransactionUseCase: CreateTransactionUseCase by inject()
    private val sendGiftAddressUseCase: SendGiftUseCase by inject()
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

    private val realm: Realm = Realm.getDefaultInstance()
    private val dbCryptoCoinModel: DbCryptoCoinModel = DbCryptoCoinModel()

    override fun injectDependency() = presenterComponent.inject(this)

    override fun createTransaction(
        context: Context,
        coinModel: CoinModel,
        phone: String,
        coinAmount: Double,
        message: String
    ) {
        this.coinFromCode = coinModel.coinId
        this.coinAmount = coinAmount
        this.message = message
        this.phone = phone
        this.phoneEncoded = "+" + phone.replace("+", "")

        mView?.showProgress(true)
        getGiftAddressUseCase.invoke(GetGiftAddressUseCase.Params(coinFromCode, phoneEncoded)) { giftAddressEither ->
            giftAddressEither.either(
                { errorResponse(it) },
                { giftAddress ->
                    this.fromAddress = giftAddress
                    createTransaction(coinFromCode, coinAmount)
                }
            )
        }
    }

    override fun completeTransaction(smsCode: String) {
        mView?.showProgress(true)
        sendGiftAddressUseCase.invoke(
            SendGiftUseCase.Params(
                smsCode = smsCode,
                hash = transactionHash,
                coinFrom = coinFromCode,
                coinFromAmount = coinAmount,
                giftId = gifMedia?.id ?: "",
                phone = phoneEncoded,
                message = message
            )
        ) { either ->
            either.either(
                { errorResponse(it) },
                {
                    mView?.showProgress(false)
                    mView?.onTransactionDone()
                }
            )
        }
    }

    private fun createTransaction(fromCoinCode: String, fromCoinAmount: Double) {
        dbCryptoCoinModel.getCryptoCoin(realm, fromCoinCode)?.let { fromCoinDb ->
            val coinDataItem = fromCoinDb.mapToDataItem()
            createTransactionUseCase.invoke(CreateTransactionUseCase.Params(coinDataItem, fromCoinAmount)) { either ->
                either.either(
                    { errorResponse(it) },
                    { hash ->
                        this.transactionHash = hash
                        mView?.openSmsCodeDialog()
                        mView?.showProgress(false)
                    }
                )
            }
        } ?: mView?.showError(R.string.error_please_try_again)
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