package com.belcobtm.presentation.screens.wallet.withdraw

import android.text.Editable
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.belcobtm.R
import com.belcobtm.domain.transaction.interactor.GetFakeSignedTransactionPlanUseCase
import com.belcobtm.domain.transaction.interactor.GetMaxValueBySignedTransactionUseCase
import com.belcobtm.domain.transaction.interactor.GetSignedTransactionPlanUseCase
import com.belcobtm.domain.transaction.interactor.GetTransactionPlanUseCase
import com.belcobtm.domain.transaction.interactor.ReceiverAccountActivatedUseCase
import com.belcobtm.domain.transaction.interactor.WithdrawUseCase
import com.belcobtm.domain.transaction.item.AmountItem
import com.belcobtm.domain.transaction.item.SignedTransactionPlanItem
import com.belcobtm.domain.transaction.item.TransactionPlanItem
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.domain.wallet.interactor.GetCoinListUseCase
import com.belcobtm.domain.wallet.item.CoinDataItem
import com.belcobtm.domain.wallet.item.isBtcCoin
import com.belcobtm.domain.wallet.item.isEthRelatedCoin
import com.belcobtm.presentation.core.coin.CoinCodeProvider
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.provider.string.StringProvider
import com.belcobtm.presentation.tools.extensions.CoinTypeExtension

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

    private val _isNextButtonEnabled = MutableLiveData<Boolean>()
    val isNextButtonEnabled: LiveData<Boolean> = _isNextButtonEnabled

    private var isAddressNotEmpty = false
    private var isAmountNotEmpty = false

    init {
        fetchInitialData()
    }

    fun fetchInitialData() {
        _loadingLiveData.value = LoadingData.Loading()
        // Kostya had some non-reproducible exception here (4 July 22)
        // Possibly https://console.firebase.google.com/project/belco-wallet/crashlytics/app/android:com.belcobtm/issues/aaf18b3400e3d9f3a297ada401ef8587?time=last-twenty-four-hours
        try {
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
                            }, onError = {
                                _fee.value = 0.0
                            })
                    }, onError = {
                        _loadingLiveData.value = LoadingData.Error(it)
                    })
                _loadingLiveData.value = LoadingData.Success(Unit)
            }, onError = {
                _loadingLiveData.value = LoadingData.Error(it)
            })
        } catch (e: Exception) {
            Log.e("WITHDRAW exception", e.message.orEmpty())
        }
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
            when {
                coinAmount <= 0 -> {
                    _cryptoAmountError.value =
                        stringProvider.getString(R.string.balance_amount_too_small)
                }
                !isSufficientBalance() -> {
                    _cryptoAmountError.value =
                        stringProvider.getString(R.string.balance_amount_exceeded)
                }
                !isSufficientEth() -> {
                    _cryptoAmountError.value =
                        stringProvider.getString(R.string.withdraw_screen_where_money_libovski)
                }
                isValidAddress(toAddress, coinCode).not() -> {
                    _addressError.value = stringProvider.getString(R.string.address_invalid)
                }
                fromCoinDataItem.code == LocalCoinType.XRP.name -> {
                    withdrawXrp(coinAmount, toAddress, transactionPlan)
                }
                else -> {
                    transactionLiveData.value = LoadingData.Loading()
                    withdrawInternal(coinAmount, toAddress, transactionPlan)
                }
            }
        }, onError = {
            transactionLiveData.value = LoadingData.Error(it)
        })
    }

    private fun withdrawXrp(
        coinAmount: Double,
        toAddress: String,
        transactionPlan: TransactionPlanItem
    ) {
        transactionLiveData.value = LoadingData.Loading()
        receiverAccountActivatedUseCase(
            ReceiverAccountActivatedUseCase.Params(toAddress, coinCode),
            onSuccess = { activated ->
                if (activated || coinAmount >= 20) {
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

    private fun withdrawInternal(
        coinAmount: Double,
        toAddress: String,
        transactionPlan: TransactionPlanItem
    ) {
        withdrawUseCase.invoke(
            params = WithdrawUseCase.Params(
                useMaxAmountFlag = _amount.value?.useMax ?: false,
                fromCoin = getCoinCode(),
                fromCoinAmount = coinAmount,
                toAddress = toAddress,
                fee = _fee.value ?: 0.0,
                fiatAmount = coinAmount * getUsdPrice(),
                transactionPlanItem = transactionPlan,
                price = getUsdPrice()
            ),
            onSuccess = {
                transactionLiveData.value = LoadingData.Success(it)
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

    fun checkAddressInput(editable: Editable?) {
        isAddressNotEmpty = editable.isNullOrEmpty().not()
        _isNextButtonEnabled.value = isAddressNotEmpty && isAmountNotEmpty
    }

    fun checkAmountInput(editable: Editable?) {
        isAmountNotEmpty = editable.isNullOrEmpty().not()
        _isNextButtonEnabled.value = isAddressNotEmpty && isAmountNotEmpty
    }

}
