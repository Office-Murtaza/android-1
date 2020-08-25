package com.app.belcobtm.ui.main.coins.send_gift

import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.transaction.interactor.SendGiftTransactionComplteUseCase
import com.app.belcobtm.domain.transaction.interactor.SendGiftTransactionCreateUseCase
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.domain.wallet.item.CoinFeeDataItem
import com.app.belcobtm.presentation.core.SingleLiveData
import com.app.belcobtm.presentation.core.mvvm.LoadingData

class SendGiftViewModel(
    private val transactionCreateUseCase: SendGiftTransactionCreateUseCase,
    private val transactionCompleteUseCase: SendGiftTransactionComplteUseCase,
    private val fromCoinDataItem: CoinDataItem,
    private val fromCoinFeeDataItem: CoinFeeDataItem,
    private val coinDataItemList: List<CoinDataItem>
) : ViewModel() {
    var transactionHash: String = ""
    private var fromCoinAmount: Double = 0.0
    private var phone: String = ""
    private var message: String = ""
    private var giftId: String = ""

    val transactionLiveData: SingleLiveData<LoadingData<SendGiftTransactionType>> = SingleLiveData()

    fun createTransaction(
        fromCoinAmount: Double,
        phone: String,
        message: String,
        giftId: String
    ) {
        transactionLiveData.value = LoadingData.Loading()
        this.fromCoinAmount = fromCoinAmount
        this.phone = "+" + phone.replace("+", "")
        this.message = message
        this.giftId = giftId
        transactionCreateUseCase.invoke(
            SendGiftTransactionCreateUseCase.Params(this.phone, fromCoinDataItem.code, this.fromCoinAmount),
            onSuccess = { hash ->
                this.transactionHash = hash
                transactionLiveData.value = LoadingData.Success(SendGiftTransactionType.CREATE)
            },
            onError = { transactionLiveData.value = LoadingData.Error(it) }
        )
    }

    fun completeTransaction(
        smsCode: String
    ) {
        transactionLiveData.value = LoadingData.Loading()
        transactionCompleteUseCase.invoke(
            SendGiftTransactionComplteUseCase.Params(
                hash = transactionHash,
                smsCode = smsCode,
                coinFrom = fromCoinDataItem.code,
                coinFromAmount = fromCoinAmount,
                phone = phone,
                message = message,
                giftId = giftId
            ),
            onSuccess = { transactionLiveData.value = LoadingData.Success(SendGiftTransactionType.COMPLETE) },
            onError = { transactionLiveData.value = LoadingData.Error(it) }
        )
    }

    fun getTransactionFee(): Double = fromCoinFeeDataItem.txFee

    fun getMaxPrice() = if ((fromCoinDataItem.code != LocalCoinType.CATM.name)
        || (coinDataItemList.find { LocalCoinType.ETH.name == it.code }?.balanceCoin ?: 0.0 >= getTransactionFee())
    ) {
        fromCoinDataItem.balanceCoin - getTransactionFee()
    } else {
        0.0
    }

    fun getCoinBalance(): Double = fromCoinDataItem.balanceCoin

    fun getUsdBalance(): Double = fromCoinDataItem.balanceUsd

    fun getUsdPrice(): Double = fromCoinDataItem.priceUsd

    fun getCoinCode(): String = fromCoinDataItem.code

    enum class SendGiftTransactionType {
        CREATE, COMPLETE
    }
}