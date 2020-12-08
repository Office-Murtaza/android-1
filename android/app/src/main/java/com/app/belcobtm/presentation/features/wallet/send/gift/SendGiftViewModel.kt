package com.app.belcobtm.presentation.features.wallet.send.gift

import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.transaction.interactor.SendGiftTransactionCreateUseCase
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.domain.wallet.item.CoinDetailsDataItem
import com.app.belcobtm.presentation.core.SingleLiveData
import com.app.belcobtm.presentation.core.coin.AmountCoinValidator
import com.app.belcobtm.presentation.core.coin.CoinCodeProvider
import com.app.belcobtm.presentation.core.coin.MinMaxCoinValueProvider
import com.app.belcobtm.presentation.core.coin.model.ValidationResult
import com.app.belcobtm.presentation.core.mvvm.LoadingData

class SendGiftViewModel(
    private val transactionCreateUseCase: SendGiftTransactionCreateUseCase,
    private val fromCoinDataItem: CoinDataItem,
    private val fromCoinDetailsDataItem: CoinDetailsDataItem,
    private val coinDataItemList: List<CoinDataItem>,
    private val minMaxCoinValueProvider: MinMaxCoinValueProvider,
    private val coinCodeProvider: CoinCodeProvider,
    private val amountCoinValidator: AmountCoinValidator
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

    fun getMinValue(): Double =
        minMaxCoinValueProvider.getMinValue(fromCoinDataItem, fromCoinDetailsDataItem)

    fun getMaxValue(): Double =
        minMaxCoinValueProvider.getMaxValue(fromCoinDataItem, fromCoinDetailsDataItem)

    fun getTransactionFee(): Double = fromCoinDetailsDataItem.txFee

    fun getCoinBalance(): Double = fromCoinDataItem.balanceCoin

    fun getUsdBalance(): Double = fromCoinDataItem.balanceUsd

    fun getUsdPrice(): Double = fromCoinDataItem.priceUsd

    fun getReservedBalanceUsd(): Double = fromCoinDataItem.reservedBalanceUsd

    fun getReservedBalanceCoin(): Double = fromCoinDataItem.reservedBalanceCoin

    fun getCoinCode(): String = coinCodeProvider.getCoinCode(fromCoinDataItem)

    fun validateAmount(amount: Double): ValidationResult =
        amountCoinValidator.validateBalance(
            amount, fromCoinDataItem, fromCoinDetailsDataItem, coinDataItemList
        )
}