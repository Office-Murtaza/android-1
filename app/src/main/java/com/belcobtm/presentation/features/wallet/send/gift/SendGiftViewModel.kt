package com.belcobtm.presentation.features.wallet.send.gift

import androidx.annotation.StringRes
import androidx.lifecycle.*
import com.belcobtm.R
import com.belcobtm.data.disk.database.account.AccountDao
import com.belcobtm.data.disk.database.service.ServiceType
import com.belcobtm.domain.Failure
import com.belcobtm.domain.service.ServiceInfoProvider
import com.belcobtm.domain.transaction.interactor.*
import com.belcobtm.domain.transaction.item.AmountItem
import com.belcobtm.domain.transaction.item.SignedTransactionPlanItem
import com.belcobtm.domain.transaction.item.TransactionPlanItem
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.domain.wallet.interactor.GetCoinListUseCase
import com.belcobtm.domain.wallet.interactor.UpdateBalanceUseCase
import com.belcobtm.domain.wallet.item.CoinDataItem
import com.belcobtm.domain.wallet.item.isBtcCoin
import com.belcobtm.domain.wallet.item.isEthRelatedCoin
import com.belcobtm.presentation.core.mvvm.LoadingData
import kotlinx.coroutines.delay
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
    private val updateBalanceUseCase: UpdateBalanceUseCase,
    private val serviceInfoProvider: ServiceInfoProvider,
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

    private val _fee = MutableLiveData(0.0)
    val fee: LiveData<Double> = _fee

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
            addSource(_fee) {
                fee = it
                processAmountWIthFee(coin, fee)
            }
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

    fun getCoinsToSelect(): List<CoinDataItem> =
        coinList.filter { coinToSend.value?.code != it.code }

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
                        coin.code, plan, useMaxAmount = false, amount = amount
                    ),
                    onSuccess = { signedTransactionPlan ->
                        signedTransactionPlanItem = signedTransactionPlan
                        _fee.value = signedTransactionPlan.fee
                    },
                    onError = { /* Failure impossible */ }
                )
            }
        }
        _amount.value = AmountItem(amount, useMax = false)
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
                _fee.value = it.fee
                _amount.value = AmountItem(it.amount, useMax = true)
            }, onError = { /* error impossible */ }
        )
    }

    fun sendGift(amount: Double, phone: String, message: String?, giftId: String?) {
        val coinToSend = coinToSend.value ?: return
        val usdAmount = amount * coinToSend.priceUsd
        val transactionPlanItem = transactionPlanItem ?: return
        val service = serviceInfoProvider.getService(ServiceType.TRANSFER)
        if (amount <= 0) {
            _cryptoAmountError.value = R.string.balance_amount_too_small
            return
        }
        if (service == null || service.txLimit < usdAmount || service.remainLimit < usdAmount) {
            _cryptoAmountError.value = R.string.limits_exceeded_validation_message
            return
        }
        val useMax = _amount.value?.useMax ?: false
        _cryptoAmountError.value = null
        _sendGiftLoadingData.value = LoadingData.Loading()
        getSignedTransactionPlanUseCase(GetSignedTransactionPlanUseCase.Params(
            toAddress, coinToSend.code, amount, transactionPlanItem, useMax
        ), onSuccess = {
            signedTransactionPlanItem = it
            _fee.value = it.fee
            if (!isSufficientBalance()) {
                _cryptoAmountError.value = R.string.insufficient_balance
                return@getSignedTransactionPlanUseCase
            }
            if (coinToSend.isEthRelatedCoin() && !isSufficientEth()) {
                _cryptoAmountError.value = R.string.send_gift_screen_where_money_libovski
                return@getSignedTransactionPlanUseCase
            }
            if (coinToSend.code == LocalCoinType.XRP.name) {
                if (toAddress == coinToSend.details.walletAddress) {
                    if (amount < 20 + (signedTransactionPlanItem?.fee ?: 0.0)) {
                        _cryptoAmountError.value = R.string.xrp_too_small_amount_error
                        _sendGiftLoadingData.value = LoadingData.DismissProgress()
                        return@getSignedTransactionPlanUseCase
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
                } else {
                    receiverAccountActivatedUseCase(
                        ReceiverAccountActivatedUseCase.Params(toAddress, coinToSend.code),
                        onSuccess = { activated ->
                            if (activated) {
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
                                if (amount < 20) {
                                    _cryptoAmountError.value = R.string.xrp_too_small_amount_error
                                    _sendGiftLoadingData.value = LoadingData.DismissProgress()
                                    return@receiverAccountActivatedUseCase
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
                            }
                        },
                        onError = { _sendGiftLoadingData.value = LoadingData.Error(it) }
                    )
                }
                if (amount < 20) {
                    _cryptoAmountError.value = R.string.xrp_too_small_amount_error
                    _sendGiftLoadingData.value = LoadingData.DismissProgress()
                    return@getSignedTransactionPlanUseCase
                } else {
                    receiverAccountActivatedUseCase(
                        ReceiverAccountActivatedUseCase.Params(toAddress, coinToSend.code),
                        onSuccess = { activated ->
                            if (activated) {
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
                            }
                        },
                        onError = { _sendGiftLoadingData.value = LoadingData.Error(it) }
                    )
                }
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
    ) {
        transactionCreateUseCase.invoke(
            params = SendGiftTransactionCreateUseCase.Params(
                useMaxAmountFlag = _amount.value?.useMax ?: false,
                amount = amount,
                coinCode = coinToSend.code,
                phone = phone,
                message = message,
                giftId = giftId,
                toAddress = toAddress,
                fee = _fee.value ?: 0.0,
                feePercent = serviceInfoProvider.getService(ServiceType.TRANSFER)?.feePercent
                    ?: 0.0,
                fiatAmount = usdAmount,
                transactionPlanItem = transactionPlanItem
            ),
            onSuccess = {
                updateBalanceUseCase(
                    UpdateBalanceUseCase.Params(
                        coinCode = coinToSend.code,
                        txAmount = usdAmount,
                        txCryptoAmount = _amount.value?.amount ?: 0.0,
                        txFee = _fee.value ?: 0.0,
                        maxAmountUsed = _amount.value?.useMax ?: false,
                    ),
                    onSuccess = {
                        _sendGiftLoadingData.value = LoadingData.Success(it)
                    },
                    onError = {
                        _sendGiftLoadingData.value = LoadingData.Error(it)
                    }
                )
            },
            onError = { _sendGiftLoadingData.value = LoadingData.Error(it) }
        )
    }

    fun selectCoin(phoneNumber: String, coinDataItem: CoinDataItem) {
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
                                _fee.value = signedTransactionPlan.fee
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
        val fee = _fee.value ?: 0.0
        val coinDataItem = _coinToSend.value ?: return false
        return if (coinDataItem.isBtcCoin()) {
            amount + fee <= signedTransactionPlanItem?.availableAmount ?: 0.0
        } else {
            amount <= coinDataItem.balanceCoin
        }
    }

    private fun isSufficientEth(): Boolean {
        val coinDataItem = _coinToSend.value ?: return false
        val ethBalance = coinList.firstOrNull {
            it.code == LocalCoinType.ETH.name
        }?.balanceCoin ?: 0.0
        return !coinDataItem.isEthRelatedCoin() || ethBalance >= (transactionPlanItem?.txFee ?: 0.0)
    }
}
