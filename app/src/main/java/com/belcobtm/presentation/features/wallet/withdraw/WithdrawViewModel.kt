package com.belcobtm.presentation.features.wallet.withdraw

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.belcobtm.R
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
import com.belcobtm.presentation.core.coin.CoinCodeProvider
import com.belcobtm.presentation.core.extensions.CoinTypeExtension
import com.belcobtm.presentation.core.extensions.getDouble
import com.belcobtm.presentation.core.extensions.getString
import com.belcobtm.presentation.core.extensions.showError
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.provider.string.StringProvider
import org.koin.android.ext.android.inject

class WithdrawViewModel(
    private val coinCode: String,
    private val getCoinListUseCase: GetCoinListUseCase,
    private val withdrawUseCase: WithdrawUseCase,
    private val getTransactionPlanUseCase: GetTransactionPlanUseCase,
    private val getSignedTransactionPlanUseCase: GetSignedTransactionPlanUseCase,
    private val getFakeSignedTransactionPlanUseCase: GetFakeSignedTransactionPlanUseCase,
    private val getMaxValueBySignedTransactionUseCase: GetMaxValueBySignedTransactionUseCase,
    private val stringProvider: StringProvider,
    private val receiverAccountActivatedUseCase: ReceiverAccountActivatedUseCase,
    private val coinCodeProvider: CoinCodeProvider,
    private val updateBalanceUseCase: UpdateBalanceUseCase,
) : ViewModel() {

    val transactionLiveData: MutableLiveData<LoadingData<Unit>> = MutableLiveData()

    private lateinit var fromCoinDataItem: CoinDataItem
    private lateinit var coinDataItemList: List<CoinDataItem>

    private val _loadingLiveData = MutableLiveData<LoadingData<Unit>>()
    val loadingLiveData: LiveData<LoadingData<Unit>>
        get() = _loadingLiveData

    private val _amount = MutableLiveData<AmountItem>()
    val amount: LiveData<AmountItem>
        get() = _amount

    private val _fee = MutableLiveData<Double>()
    val fee: LiveData<Double>
        get() = _fee

    private val _cryptoAmountError = MutableLiveData<String?>()
    val cryptoAmountError: LiveData<String?>
        get() = _cryptoAmountError

    private val _addressError = MutableLiveData<String?>()
    val addressError: LiveData<String?>
        get() = _addressError

    private var transactionPlan: TransactionPlanItem? = null
    private var signedTransactionPlanItem: SignedTransactionPlanItem? = null

    init {
        fetchInitialData()
    }

    fun fetchInitialData() {
        _loadingLiveData.value = LoadingData.Loading()
        getCoinListUseCase.invoke(Unit, onSuccess = { coins ->
            coinDataItemList = coins
            fromCoinDataItem = coinDataItemList.find { it.code == coinCode }
                ?: throw IllegalStateException("Invalid coin code that is not presented in a list $coinCode")
            getTransactionPlanUseCase(
                fromCoinDataItem.code,
                onSuccess = { transactionPlan ->
                    this.transactionPlan = transactionPlan
                    getFakeSignedTransactionPlanUseCase(
                        GetFakeSignedTransactionPlanUseCase.Params(
                            fromCoinDataItem.code,
                            transactionPlan,
                            useMaxAmount = false
                        ),
                        onSuccess = { signedTransactionPlan ->
                            signedTransactionPlanItem = signedTransactionPlan
                            _fee.value = signedTransactionPlan.fee
                            _loadingLiveData.value = LoadingData.Success(Unit)
                        }, onError = {
                            _loadingLiveData.value = LoadingData.Error(it)
                        })
                }, onError = {
                    _loadingLiveData.value = LoadingData.Error(it)
                })
        }, onError = {
            _loadingLiveData.value = LoadingData.Error(it)
        })
    }

    fun withdraw(toAddress: String) {
        val coinAmount = _amount.value?.amount ?: 0.0
        val transactionPlan = transactionPlan ?: return
        val useMax = _amount.value?.useMax ?: false
        _cryptoAmountError.value = null
        getSignedTransactionPlanUseCase(GetSignedTransactionPlanUseCase.Params(
            toAddress, fromCoinDataItem.code, coinAmount, transactionPlan, useMax
        ), onSuccess = { signedTransactionPlan ->
            _fee.value = signedTransactionPlan.fee
            signedTransactionPlanItem = signedTransactionPlan
            if (coinAmount <= 0) {
                _cryptoAmountError.value =
                    stringProvider.getString(R.string.balance_amount_too_small)
                return@getSignedTransactionPlanUseCase
            }

            if (!isSufficientBalance()) {
                _cryptoAmountError.value =
                    stringProvider.getString(R.string.balance_amount_exceeded)
                return@getSignedTransactionPlanUseCase
            }

            if (!isSufficientEth()) {
                _cryptoAmountError.value =
                    stringProvider.getString(R.string.withdraw_screen_where_money_libovski)
                return@getSignedTransactionPlanUseCase
            }
            if (!isValidAddress(toAddress, coinCode)) {
                _addressError.value = stringProvider.getString(R.string.address_invalid)
                return@getSignedTransactionPlanUseCase
            }
            transactionLiveData.value = LoadingData.Loading()
            if (fromCoinDataItem.code == LocalCoinType.XRP.name) {
                if (coinAmount < 20) {
                    _cryptoAmountError.value =
                        stringProvider.getString(R.string.xrp_too_small_amount_error)
                    transactionLiveData.value = LoadingData.DismissProgress()
                    return@getSignedTransactionPlanUseCase
                } else {
                    receiverAccountActivatedUseCase(
                        ReceiverAccountActivatedUseCase.Params(toAddress, coinCode),
                        onSuccess = { activated ->
                            if (activated) {
                                withdrawInternal(coinAmount, toAddress, transactionPlan)
                            } else {
                                _cryptoAmountError.value =
                                    stringProvider.getString(R.string.xrp_too_small_amount_error)
                                transactionLiveData.value = LoadingData.DismissProgress()
                            }
                        },
                        onError = { transactionLiveData.value = LoadingData.Error(it) }
                    )
                }
            } else {
                withdrawInternal(coinAmount, toAddress, transactionPlan)
            }
        }, onError = {
            transactionLiveData.value = LoadingData.Error(it)
        })
    }

    private fun withdrawInternal(
        coinAmount: Double,
        toAddress: String,
        transactionPlan: TransactionPlanItem
    ) {
        withdrawUseCase.invoke(
            params = WithdrawUseCase.Params(
                _amount.value?.useMax ?: false,
                getCoinCode(),
                coinAmount,
                toAddress,
                _fee.value ?: 0.0,
                transactionPlan
            ),
            onSuccess = {
                updateBalanceUseCase(
                    UpdateBalanceUseCase.Params(
                        coinCode = getCoinCode(),
                        txAmount = coinAmount * getUsdPrice(),
                        txCryptoAmount = coinAmount,
                        txFee = _fee.value ?: 0.0,
                        maxAmountUsed = amount.value?.useMax ?: false,
                    ),
                    onSuccess = {
                        transactionLiveData.value = LoadingData.Success(it)
                    },
                    onError = {
                        transactionLiveData.value = LoadingData.Error(it)
                    }
                )
            },
            onError = { transactionLiveData.value = LoadingData.Error(it) }
        )
    }

    fun setAmount(amount: Double) {
        if (_amount.value?.useMax == true) {
            transactionPlan?.let { plan ->
                getFakeSignedTransactionPlanUseCase(
                    GetFakeSignedTransactionPlanUseCase.Params(
                        fromCoinDataItem.code, plan, useMaxAmount = false, amount = amount
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
        val transactionPlan = transactionPlan ?: return
        if (_amount.value?.useMax == true) {
            return
        }
        getMaxValueBySignedTransactionUseCase(
            GetMaxValueBySignedTransactionUseCase.Params(
                transactionPlan,
                fromCoinDataItem,
            ),
            onSuccess = {
                _fee.value = it.fee
                _amount.value = AmountItem(it.amount, useMax = true)
            }, onError = { /* error impossible */ }
        )
    }

    fun getCoinBalance(): Double = fromCoinDataItem.balanceCoin

    fun getUsdBalance(): Double = fromCoinDataItem.balanceUsd

    fun getUsdPrice(): Double = fromCoinDataItem.priceUsd

    fun getReservedBalanceUsd(): Double = fromCoinDataItem.reservedBalanceUsd

    fun getReservedBalanceCoin(): Double = fromCoinDataItem.reservedBalanceCoin

    fun getCoinCode(): String = coinCode

    private fun isSufficientBalance(): Boolean {
        val amount = _amount.value?.amount ?: 0.0
        val fee = _fee.value ?: 0.0
        return if (fromCoinDataItem.isBtcCoin()) {
            amount + fee <= signedTransactionPlanItem?.availableAmount ?: 0.0
        } else {
            amount <= fromCoinDataItem.balanceCoin
        }
    }

    private fun isSufficientEth(): Boolean {
        val ethBalance = coinDataItemList.firstOrNull {
            it.code == LocalCoinType.ETH.name
        }?.balanceCoin ?: 0.0
        return !fromCoinDataItem.isEthRelatedCoin() || ethBalance >= (transactionPlan?.txFee ?: 0.0)
    }

    private fun isValidAddress(address: String, coin: String): Boolean {
        val coinCode = coinCodeProvider.getCoinCode(coin)
        val coinType = CoinTypeExtension.getTypeByCode(coinCode)
        return coinType?.validate(address) ?: false
    }
}
