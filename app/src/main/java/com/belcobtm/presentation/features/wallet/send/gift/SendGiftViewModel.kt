package com.belcobtm.presentation.features.wallet.send.gift

import androidx.annotation.StringRes
import androidx.lifecycle.*
import com.belcobtm.R
import com.belcobtm.data.disk.database.account.AccountDao
import com.belcobtm.domain.Failure
import com.belcobtm.domain.transaction.interactor.GetTransactionPlanUseCase
import com.belcobtm.domain.transaction.interactor.GetTransferAddressUseCase
import com.belcobtm.domain.transaction.interactor.SendGiftTransactionCreateUseCase
import com.belcobtm.domain.transaction.item.TransactionPlanItem
import com.belcobtm.domain.wallet.interactor.GetCoinListUseCase
import com.belcobtm.domain.wallet.item.CoinDataItem
import com.belcobtm.domain.wallet.item.isEthRelatedCoin
import com.belcobtm.presentation.core.coin.AmountCoinValidator
import com.belcobtm.presentation.core.coin.CoinLimitsValueProvider
import com.belcobtm.presentation.core.coin.model.ValidationResult
import com.belcobtm.presentation.core.mvvm.LoadingData
import kotlinx.coroutines.launch

class SendGiftViewModel(
    private val transactionCreateUseCase: SendGiftTransactionCreateUseCase,
    private val getCoinListUseCase: GetCoinListUseCase,
    private val accountDao: AccountDao,
    private val coinLimitsValueProvider: CoinLimitsValueProvider,
    private val getTransactionPlanUseCase: GetTransactionPlanUseCase,
    private val getTransferAddressUseCase: GetTransferAddressUseCase,
    private val amountCoinValidator: AmountCoinValidator
) : ViewModel() {

    private lateinit var coinList: List<CoinDataItem>
    private lateinit var toAddress: String
    private var transactionPlanItem: TransactionPlanItem? = null

    private val _initialLoadingData = MutableLiveData<LoadingData<Unit>>()
    val initialLoadingData: LiveData<LoadingData<Unit>> = _initialLoadingData

    private val _transactionPlanLiveData = MutableLiveData<LoadingData<Unit>>()
    val transactionPlanLiveData: LiveData<LoadingData<Unit>> = _transactionPlanLiveData

    private val _sendGiftLoadingData = MutableLiveData<LoadingData<Unit>>()
    val sendGiftLoadingData: LiveData<LoadingData<Unit>> = _sendGiftLoadingData

    private val _coinToSend = MutableLiveData<CoinDataItem>()
    val coinToSend: LiveData<CoinDataItem> = _coinToSend

    private val _fee = MutableLiveData<Double>()
    val fee: LiveData<Double> = _fee

    private val _cryptoAmountError = MutableLiveData<@StringRes Int?>()
    val cryptoAmountError: LiveData<Int?> = _cryptoAmountError

    private val _sendCoinAmount = MutableLiveData(0.0)
    val sendCoinAmount: LiveData<Double> = _sendCoinAmount

    val usdAmount: LiveData<Double>
        get() = MediatorLiveData<Double>().apply {
            var cryptoAmount: Double? = null
            var coinData: CoinDataItem? = null
            addSource(sendCoinAmount) {
                cryptoAmount = it
                processCoinItem(this, cryptoAmount, coinData)
            }
            addSource(coinToSend) {
                coinData = it
                processCoinItem(this, cryptoAmount, coinData)
            }
        }

    private fun processCoinItem(
        liveData: MediatorLiveData<Double>,
        cryptoAmount: Double?,
        coinData: CoinDataItem?
    ) {
        if (cryptoAmount != null && coinData != null) {
            liveData.value = cryptoAmount * coinData.priceUsd
        }
    }

    fun getCoinsToSelect(): List<CoinDataItem> =
        coinList.filter { coinToSend.value?.code != it.code }

    fun updateAmountToSend(amount: Double) {
        _sendCoinAmount.value = amount
    }

    fun fetchInitialData(phoneNumber: String) {
        viewModelScope.launch {
            _initialLoadingData.value = LoadingData.Loading(Unit)
            val allCoins = accountDao.getItemList().orEmpty()
            if (allCoins.isNotEmpty()) {
                getCoinListUseCase(
                    params = Unit,
                    onSuccess = { coinsDataList ->
                        coinList = coinsDataList
                        val coin = coinList.firstOrNull()
                        if (coin == null) {
                            _initialLoadingData.value = LoadingData.Error(Failure.ServerError())
                        } else {
                            getTransferAddressUseCase(
                                GetTransferAddressUseCase.Params(phoneNumber, coin.code),
                                onSuccess = { address ->
                                    toAddress = address
                                    getTransactionPlanUseCase(
                                        coin.code,
                                        onSuccess = { transactionPlan ->
                                            transactionPlanItem = transactionPlan
                                            _fee.value = resolveFee(coin)
                                            updateCoinInfo(coin)
                                            _initialLoadingData.value = LoadingData.Success(Unit)
                                        },
                                        onError = {
                                            _initialLoadingData.value = LoadingData.Error(it)
                                        }
                                    )
                                }, onError = {
                                    _initialLoadingData.value = LoadingData.Error(it)
                                })
                        }
                    },
                    onError = {
                        _initialLoadingData.value = LoadingData.Error(Failure.ServerError())
                    }
                )
            } else {
                _initialLoadingData.value = LoadingData.Error(Failure.ServerError())
            }
        }
    }

    fun sendGift(amount: Double, phone: String, message: String?, giftId: String?) {
        val coinToSend = coinToSend.value ?: return
        val transactionPlanItem = transactionPlanItem ?: return
        if (amount <= 0) {
            _cryptoAmountError.value = R.string.balance_amount_too_small
            return
        }
        val amountValidationResult = amountCoinValidator.validateBalance(
            amount, coinToSend, coinList
        )
        if (amountValidationResult is ValidationResult.InValid) {
            _cryptoAmountError.value = amountValidationResult.error
            return
        }
        _cryptoAmountError.value = null
        _sendGiftLoadingData.value = LoadingData.Loading()
        transactionCreateUseCase.invoke(
            params = SendGiftTransactionCreateUseCase.Params(
                amount = amount,
                coinCode = coinToSend.code,
                phone = phone,
                message = message,
                giftId = giftId,
                toAddress = toAddress,
                fee = _fee.value ?: 0.0,
                transactionPlanItem = transactionPlanItem
            ),
            onSuccess = { _sendGiftLoadingData.value = LoadingData.Success(it) },
            onError = { _sendGiftLoadingData.value = LoadingData.Error(it) }
        )
    }

    fun setMaxCoinAmount() {
        val currentCoinToSend = coinToSend.value ?: return
        val transactionPlanItem = transactionPlanItem ?: return
        val maxAmount = coinLimitsValueProvider
            .getMaxValue(currentCoinToSend, transactionPlanItem.txFee)
        updateAmountToSend(maxAmount)
    }

    fun selectCoin(coinDataItem: CoinDataItem) {
        updateCoinInfo(coinDataItem)
    }

    private fun updateCoinInfo(coinToSend: CoinDataItem) {
        _coinToSend.value = coinToSend
    }

    private fun resolveFee(coinDataItem: CoinDataItem): Double =
        if (coinDataItem.isEthRelatedCoin()) {
            transactionPlanItem?.nativeTxFee ?: 0.0
        } else {
            transactionPlanItem?.txFee ?: 0.0
        }
}
