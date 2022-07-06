package com.belcobtm.presentation.screens.bank_accounts.payments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.belcobtm.domain.transaction.interactor.GetTransactionPlanUseCase
import com.belcobtm.domain.transaction.item.TransactionPlanItem
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.domain.wallet.interactor.GetCoinByCodeUseCase
import com.belcobtm.domain.wallet.item.CoinDataItem

class PaymentBuyUsdcViewModel(
    private val getTransactionPlanUseCase: GetTransactionPlanUseCase,
    private val getCoinByCodeUseCase: GetCoinByCodeUseCase,
) : ViewModel() {

    private val _convertedValued = MutableLiveData<Double>()
    val convertedValued: LiveData<Double> = _convertedValued

    private val _platformFee = MutableLiveData<Double>()
    val platformFee: LiveData<Double> = _platformFee

    private val _usdcExchangeValue = MutableLiveData<Double>()
    val usdcExchangeValue: LiveData<Double> = _usdcExchangeValue

    private val _usdcNetworkFee = MutableLiveData<Double>()
    val usdcNetworkFee: LiveData<Double> = _usdcNetworkFee

    lateinit var usdcDataItem: CoinDataItem
    var amount: Double = 0.0
    private lateinit var usdcTransactionPlanItem: TransactionPlanItem

    init {
        getUsdcInfo()
    }

    fun computeConvertedValue(amount: Double, platformFeePrecent: Double) {
        // $ to USDC
        if (::usdcDataItem.isInitialized && ::usdcTransactionPlanItem.isInitialized) {
            val platformFee = amount / usdcDataItem.priceUsd * (platformFeePrecent / 100)
            _platformFee.value = platformFee
            _convertedValued.value =
                amount / usdcDataItem.priceUsd - platformFee - usdcTransactionPlanItem.nativeTxFee
        }
    }

    fun getUsdcInfo() {
        getCoinByCodeUseCase.invoke(LocalCoinType.USDC.name, onSuccess = { usdc ->
            usdcDataItem = usdc
            _usdcExchangeValue.value = usdcDataItem.priceUsd
            getTransactionPlanUseCase.invoke(LocalCoinType.USDC.name, onSuccess = { usdcPlan ->
                usdcTransactionPlanItem = usdcPlan
                _usdcNetworkFee.value = usdcTransactionPlanItem.nativeTxFee
            }, onError = {
            })
        }, onError = {
        })

    }
}