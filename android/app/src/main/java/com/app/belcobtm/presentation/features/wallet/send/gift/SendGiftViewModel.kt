package com.app.belcobtm.presentation.features.wallet.send.gift

import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.transaction.interactor.SendGiftTransactionCreateUseCase
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.domain.wallet.item.CoinFeeDataItem
import com.app.belcobtm.presentation.core.SingleLiveData
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import kotlin.math.max

class SendGiftViewModel(
    private val transactionCreateUseCase: SendGiftTransactionCreateUseCase,
    private val fromCoinDataItem: CoinDataItem,
    private val fromCoinFeeDataItem: CoinFeeDataItem
) : ViewModel() {
    val sendGiftLiveData: SingleLiveData<LoadingData<Unit>> = SingleLiveData()

    fun sendGift(
        amount: Double,
        phone: String,
        message: String,
        giftId: String
    ) {
        sendGiftLiveData.value = LoadingData.Loading()
        transactionCreateUseCase.invoke(
            params = SendGiftTransactionCreateUseCase.Params(
                amount = amount,
                coinCode = fromCoinDataItem.code,
                phone = phone,
                message = message,
                giftId = giftId
            ),
            onSuccess = { sendGiftLiveData.value = LoadingData.Success(it) },
            onError = { sendGiftLiveData.value = LoadingData.Error(it) }
        )
    }

    fun getMaxValue(): Double = when (getCoinCode()) {
        LocalCoinType.CATM.name -> getCoinBalance()
        LocalCoinType.XRP.name -> max(0.0, getCoinBalance() - getTransactionFee() - 20)
        else -> max(0.0, getCoinBalance() - getTransactionFee())
    }

    fun getTransactionFee(): Double = fromCoinFeeDataItem.txFee

    fun getCoinBalance(): Double = fromCoinDataItem.balanceCoin

    fun getUsdBalance(): Double = fromCoinDataItem.balanceUsd

    fun getUsdPrice(): Double = fromCoinDataItem.priceUsd

    fun getCoinCode(): String = fromCoinDataItem.code
}