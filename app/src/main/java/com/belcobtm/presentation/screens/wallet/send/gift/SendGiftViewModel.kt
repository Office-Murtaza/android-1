package com.belcobtm.presentation.screens.wallet.send.gift

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belcobtm.R
import com.belcobtm.data.disk.database.account.AccountDao
import com.belcobtm.domain.Failure
import com.belcobtm.domain.service.ServiceInfoProvider
import com.belcobtm.domain.service.ServiceType
import com.belcobtm.domain.transaction.interactor.GetFakeSignedTransactionPlanUseCase
import com.belcobtm.domain.transaction.interactor.GetMaxValueBySignedTransactionUseCase
import com.belcobtm.domain.transaction.interactor.GetSignedTransactionPlanUseCase
import com.belcobtm.domain.transaction.interactor.GetTransactionPlanUseCase
import com.belcobtm.domain.transaction.interactor.GetTransferAddressUseCase
import com.belcobtm.domain.transaction.interactor.ReceiverAccountActivatedUseCase
import com.belcobtm.domain.transaction.interactor.SendGiftTransactionCreateUseCase
import com.belcobtm.domain.transaction.item.AmountItem
import com.belcobtm.domain.transaction.item.SignedTransactionPlanItem
import com.belcobtm.domain.transaction.item.TransactionPlanItem
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.domain.wallet.interactor.GetCoinListUseCase
import com.belcobtm.domain.wallet.item.CoinDataItem
import com.belcobtm.domain.wallet.item.isBtcCoin
import com.belcobtm.domain.wallet.item.isEthRelatedCoin
import com.belcobtm.presentation.core.livedata.DoubleCombinedLiveData
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.provider.string.StringProvider
import kotlinx.coroutines.launch

class SendGiftViewModel(
    private val transactionCreateUseCase: SendGiftTransactionCreateUseCase,
    private val getCoinListUseCase: GetCoinListUseCase,
    private val accountDao: AccountDao,
    private val getTransactionPlanUseCase: GetTransactionPlanUseCase,
    private val getTransferAddressUseCase: GetTransferAddressUseCase,
    private val getSignedTransactionPlanUseCase: GetSignedTransactionPlanUseCase,
    private val getFakeSignedTransactionPlanUseCase: GetFakeSignedTransactionPlanUseCase,
    private val getMaxValueBySignedTransactionUseCase: GetMaxValueBySignedTransactionUseCase,
    private val receiverAccountActivatedUseCase: ReceiverAccountActivatedUseCase,
    private val serviceInfoProvider: ServiceInfoProvider,
    private val stringProvider: StringProvider
) : ViewModel() {

    private lateinit var coinList: List<CoinDataItem>
    private lateinit var toAddress: String
    private var transactionPlanItem: TransactionPlanItem? = null
    private var signedTransactionPlanItem: SignedTransactionPlanItem? = null

    private val _initialLoadingData = MutableLiveData<LoadingData<Unit>>()
    val initialLoadingData: LiveData<LoadingData<Unit>> = _initialLoadingData

    private val _transactionPlanLiveData = MutableLiveData<LoadingData<Unit>>()
    val transactionPlanLiveData: LiveData<LoadingData<Unit>> = _transactionPlanLiveData

    private val _sendGiftLoadingData = MutableLiveData<LoadingData<Unit>>()
    val sendGiftLoadingData: LiveData<LoadingData<Unit>> = _sendGiftLoadingData

    private val _coinToSend = MutableLiveData<CoinDataItem>()
    val coinToSend: LiveData<CoinDataItem> = _coinToSend

    private val feeLiveData = MutableLiveData(0.0)

    private val _amount = MutableLiveData<AmountItem>()
    val amount: LiveData<AmountItem> = _amount

    private val _cryptoAmountError = MutableLiveData<@StringRes Int?>()
    val cryptoAmountError: LiveData<Int?> = _cryptoAmountError

    val usdAmount: LiveData<Double>
        get() = MediatorLiveData<Double>().apply {
            var cryptoAmount: Double? = null
            var coinData: CoinDataItem? = null
            addSource(_amount) {
                cryptoAmount = it.amount
                processCoinItem(cryptoAmount, coinData)
            }
            addSource(coinToSend) {
                coinData = it
                processCoinItem(cryptoAmount, coinData)
            }
        }

    val coinWithFee: LiveData<Pair<CoinDataItem, Double>>
        get() = MediatorLiveData<Pair<CoinDataItem, Double>>().apply {
            var coin: CoinDataItem? = null
            var fee: Double? = null
            addSource(coinToSend) {
                coin = it
                processAmountWIthFee(coin, fee)
            }
            addSource(feeLiveData) {
                fee = it
                processAmountWIthFee(coin, fee)
            }
        }

    private val feePercent = MutableLiveData(0.0)

    val giftFee: LiveData<GiftFeeModelView> =
        DoubleCombinedLiveData(usdAmount, coinToSend) { _, coin ->
            GiftFeeModelView(
                platformFeePercents = feePercent.value ?: 0.0,
                platformFeeCoinAmount = (amount.value?.amount ?: 0.0) * (feePercent.value ?: 0.0) / 100.0,
                swapCoinCode = coin?.code.orEmpty()
            )
        }

    private fun MediatorLiveData<Pair<CoinDataItem, Double>>.processAmountWIthFee(
        coin: CoinDataItem?,
        fee: Double?
    ) {
        if (coin != null && fee != null) {
            value = Pair(coin, fee)
        }
    }

    private fun MediatorLiveData<Double>.processCoinItem(
        cryptoAmount: Double?,
        coinData: CoinDataItem?
    ) {
        if (cryptoAmount != null && coinData != null) {
            value = cryptoAmount * coinData.priceUsd
        }
    }

    init {
        viewModelScope.launch {
            feePercent.value = serviceInfoProvider.getService(ServiceType.TRANSFER)?.feePercent ?: 0.0
        }
    }

    fun getCoinsToSelect(): List<CoinDataItem> =
        coinList.filter { coinToSend.value?.code != it.code }

    fun fetchInitialData(phoneNumber: String) {
        viewModelScope.launch {
            _initialLoadingData.value = LoadingData.Loading(Unit)
            val allCoins = accountDao.getAvailableAccounts().orEmpty()
            if (allCoins.isNotEmpty()) {
                getCoinListUseCase(
                    params = Unit,
                    onSuccess = { coinsDataList ->
                        coinList = coinsDataList
                        val coin = coinList.firstOrNull()
                        if (coin == null) {
                            _initialLoadingData.value = LoadingData.Error(Failure.ServerError())
                        } else {
                            coin.let(_coinToSend::setValue)
                            fetchTransactionPlan(phoneNumber, coin, _initialLoadingData)
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

    fun getCoinCode(): String = _coinToSend.value?.code.orEmpty()

    fun setAmount(amount: Double) {
        val coin = _coinToSend.value ?: return
        if (_amount.value?.useMax == true) {
            transactionPlanItem?.let { plan ->
                getFakeSignedTransactionPlanUseCase(
                    GetFakeSignedTransactionPlanUseCase.Params(
                        coin.code,
                        plan,
                        useMaxAmount = false,
                        amount = amount
                    ),
                    onSuccess = { signedTransactionPlan ->
                        signedTransactionPlanItem = signedTransactionPlan
                        feeLiveData.value = signedTransactionPlan.fee
                    },
                    onError = { /* Failure impossible */ }
                )
            }
        }
        _amount.value = AmountItem(amount)
    }

    fun setMaxAmount() {
        val transactionPlan = transactionPlanItem ?: return
        val coin = _coinToSend.value ?: return
        if (_amount.value?.useMax == true) {
            return
        }
        getMaxValueBySignedTransactionUseCase(
            GetMaxValueBySignedTransactionUseCase.Params(transactionPlan, coin),
            onSuccess = {
                feeLiveData.value = it.fee
                _amount.value = AmountItem(it.amount, useMax = true)
            }, onError = { /* error impossible */ }
        )
    }

    fun sendGift(amount: Double, phone: String, message: String?, giftId: String?) = viewModelScope.launch {
        val coinToSend = coinToSend.value ?: return@launch
        val usdAmount = amount * coinToSend.priceUsd
        val transactionPlanItem = transactionPlanItem ?: return@launch
        val service = serviceInfoProvider.getService(ServiceType.TRANSFER)
        if (amount <= 0) {
            _cryptoAmountError.value = R.string.balance_amount_too_small
            return@launch
        }
        if (service == null || service.txLimit < usdAmount || service.remainLimit < usdAmount) {
            _cryptoAmountError.value = R.string.limits_exceeded_validation_message
            return@launch
        }
        val useMax = _amount.value?.useMax ?: false
        _cryptoAmountError.value = null
        _sendGiftLoadingData.value = LoadingData.Loading()
        getSignedTransactionPlanUseCase(GetSignedTransactionPlanUseCase.Params(
            toAddress, coinToSend.code, amount, transactionPlanItem, useMax
        ), onSuccess = {
            signedTransactionPlanItem = it
            feeLiveData.value = it.fee
            if (!isSufficientBalance()) {
                _cryptoAmountError.value = R.string.insufficient_balance
                return@getSignedTransactionPlanUseCase
            }
            if (coinToSend.isEthRelatedCoin() && !isSufficientEth()) {
                _cryptoAmountError.value = R.string.send_gift_screen_where_money_libovski
                return@getSignedTransactionPlanUseCase
            }
            if (coinToSend.code == LocalCoinType.XRP.name) {
                receiverAccountActivatedUseCase(
                    ReceiverAccountActivatedUseCase.Params(toAddress, coinToSend.code),
                    onSuccess = { activated ->
                        if (activated || amount > 20) {
                            sendGiftInternal(
                                amount,
                                usdAmount,
                                coinToSend,
                                phone,
                                message,
                                giftId,
                                transactionPlanItem
                            )
                        } else {
                            _cryptoAmountError.value = R.string.xrp_too_small_amount_error
                            _sendGiftLoadingData.value = LoadingData.DismissProgress()
                            return@receiverAccountActivatedUseCase
                        }
                    },
                    onError = { error -> _sendGiftLoadingData.value = LoadingData.Error(error) }
                )
            } else {
                sendGiftInternal(
                    amount,
                    usdAmount,
                    coinToSend,
                    phone,
                    message,
                    giftId,
                    transactionPlanItem
                )
            }
        }, onError = {
            _sendGiftLoadingData.value = LoadingData.Error(it)
        })
    }

    private fun sendGiftInternal(
        amount: Double,
        usdAmount: Double,
        coinToSend: CoinDataItem,
        phone: String,
        message: String?,
        giftId: String?,
        transactionPlanItem: TransactionPlanItem
    ) = viewModelScope.launch {
        transactionCreateUseCase.invoke(
            params = SendGiftTransactionCreateUseCase.Params(
                useMaxAmountFlag = _amount.value?.useMax ?: false,
                amount = amount,
                coinCode = coinToSend.code,
                phone = phone,
                message = message,
                giftId = giftId,
                toAddress = toAddress,
                price = coinToSend.priceUsd,
                fee = feeLiveData.value ?: 0.0,
                feePercent = serviceInfoProvider.getService(ServiceType.TRANSFER)?.feePercent
                    ?: 0.0,
                fiatAmount = usdAmount,
                transactionPlanItem = transactionPlanItem
            ),
            onSuccess = {
                _sendGiftLoadingData.value = LoadingData.Success(it)
            },
            onError = { _sendGiftLoadingData.value = LoadingData.Error(it) }
        )
    }

    fun selectCoin(phoneNumber: String, coinDataItem: CoinDataItem) {
        _amount.value = AmountItem()
        _coinToSend.value = coinDataItem
        _transactionPlanLiveData.value = LoadingData.Loading()
        fetchTransactionPlan(phoneNumber, coinDataItem, _transactionPlanLiveData)
    }

    private fun fetchTransactionPlan(
        phoneNumber: String,
        coin: CoinDataItem,
        loadingLiveData: MutableLiveData<LoadingData<Unit>>
    ) {
        getTransferAddressUseCase(
            GetTransferAddressUseCase.Params(phoneNumber, coin.code),
            onSuccess = { address ->
                toAddress = address
                getTransactionPlanUseCase(
                    coin.code,
                    onSuccess = { transactionPlan ->
                        transactionPlanItem = transactionPlan
                        getFakeSignedTransactionPlanUseCase(
                            GetFakeSignedTransactionPlanUseCase.Params(
                                coin.code,
                                transactionPlan,
                                useMaxAmount = false
                            ),
                            onSuccess = { signedTransactionPlan ->
                                signedTransactionPlanItem = signedTransactionPlan
                                feeLiveData.value = signedTransactionPlan.fee
                                _coinToSend.value = coin
                                loadingLiveData.value = LoadingData.Success(Unit)
                            }, onError = {
                                loadingLiveData.value = LoadingData.Error(it)
                            })
                        loadingLiveData.value = LoadingData.Success(Unit)
                    },
                    onError = {
                        loadingLiveData.value = LoadingData.Error(it)
                    }
                )
            }, onError = {
                loadingLiveData.value = LoadingData.Error(it)
            })
    }

    private fun isSufficientBalance(): Boolean {
        val amount = _amount.value?.amount ?: 0.0
        val fee = feeLiveData.value ?: 0.0
        val coinDataItem = _coinToSend.value ?: return false
        return if (coinDataItem.isBtcCoin()) {
            amount + fee <= signedTransactionPlanItem?.availableAmount ?: 0.0
        } else {
            amount <= coinDataItem.balanceCoin
        }
    }

    fun showLocationError() {
        _sendGiftLoadingData.value = LoadingData.Error(
            Failure.LocationError(
                stringProvider.getString(R.string.location_required_on_trade_creation)
            )
        )
    }

    private fun isSufficientEth(): Boolean {
        val coinDataItem = _coinToSend.value ?: return false
        val ethBalance = coinList.firstOrNull {
            it.code == LocalCoinType.ETH.name
        }?.balanceCoin ?: 0.0
        return !coinDataItem.isEthRelatedCoin() || ethBalance >= (transactionPlanItem?.txFee ?: 0.0)
    }

}

data class GiftFeeModelView(
    val platformFeePercents: Double,
    val platformFeeCoinAmount: Double,
    val swapCoinCode: String
)
