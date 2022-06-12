package com.belcobtm.presentation.features.bank_accounts.payments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.belcobtm.domain.transaction.interactor.GetTransactionPlanUseCase
import com.belcobtm.domain.transaction.item.TransactionPlanItem
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.domain.wallet.interactor.GetCoinByCodeUseCase
import com.belcobtm.domain.wallet.item.CoinDataItem

class PaymentSellUsdcViewModel(
    private val getTransactionPlanUseCase: GetTransactionPlanUseCase,
    private val getCoinByCodeUseCase: GetCoinByCodeUseCase,
) : ViewModel() {

    private val _convertedValued = MutableLiveData<Double>()
    val convertedValued: LiveData<Double> = _convertedValued

    private val _platformFee = MutableLiveData<Double>()
    val platformFee: LiveData<Double> = _platformFee

    private val _usdcExchangeValue = MutableLiveData<Double>()
    val usdcExchangeValue: LiveData<Double> = _usdcExchangeValue

    private val _ethNetworkFee = MutableLiveData<Double>()
    val ethNetworkFee: LiveData<Double> = _ethNetworkFee

    private val _usdcBalance = MutableLiveData<Double>()
    val usdcBalance: LiveData<Double> = _usdcBalance

    lateinit var usdcDataItem: CoinDataItem
    lateinit var ethDataItem: CoinDataItem
    var amount: Double = 0.0
    private lateinit var usdcTransactionPlanItem: TransactionPlanItem

    init {
        getUsdcInfo()
    }

    fun computeConvertedValue(amount: Double, platformFeePrecent: Double) {
        if (::usdcDataItem.isInitialized) {
            _platformFee.value = amount * usdcDataItem.priceUsd * (platformFeePrecent / 100)
            _convertedValued.value =
                amount * usdcDataItem.priceUsd * (1 - platformFeePrecent / 100)
        }
    }

    fun getUsdcInfo() {
        getCoinByCodeUseCase.invoke(LocalCoinType.USDC.name, onSuccess = { usdc ->
            usdcDataItem = usdc
            _usdcExchangeValue.value = usdcDataItem.priceUsd
            _usdcBalance.value = usdcDataItem.balanceCoin
        }, onError = {
        })
        getCoinByCodeUseCase.invoke(LocalCoinType.ETH.name, onSuccess = { usdc ->
            ethDataItem = usdc
        }, onError = {
        })
        getTransactionPlanUseCase.invoke(LocalCoinType.USDC.name, onSuccess = { usdcPlan ->
            usdcTransactionPlanItem = usdcPlan
            _ethNetworkFee.value = usdcTransactionPlanItem.txFee
        }, onError = {
        })


    }
}