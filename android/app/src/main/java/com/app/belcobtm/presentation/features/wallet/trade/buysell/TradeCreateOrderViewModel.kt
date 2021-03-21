package com.app.belcobtm.presentation.features.wallet.trade.buysell

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.R
import com.app.belcobtm.data.model.trade.TradeType
import com.app.belcobtm.domain.trade.details.GetTradeDetailsUseCase
import com.app.belcobtm.domain.trade.order.CreateOrderUseCase
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.domain.wallet.interactor.GetCoinByCodeUseCase
import com.app.belcobtm.domain.wallet.interactor.GetCoinDetailsUseCase
import com.app.belcobtm.presentation.core.formatter.Formatter
import com.app.belcobtm.presentation.core.livedata.DoubleCombinedLiveData
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.parser.StringParser
import com.app.belcobtm.presentation.core.provider.string.StringProvider
import com.app.belcobtm.presentation.features.wallet.trade.buysell.model.TradeCryptoAmount
import com.app.belcobtm.presentation.features.wallet.trade.buysell.model.TradeFee
import com.app.belcobtm.presentation.features.wallet.trade.buysell.model.TradeOrderItem
import com.app.belcobtm.presentation.features.wallet.trade.list.model.TradeItem

class TradeCreateOrderViewModel(
    private val getTradeDetailsUseCase: GetTradeDetailsUseCase,
    private val getCoinDetailsUseCase: GetCoinDetailsUseCase,
    private val getCoinByCodeUseCase: GetCoinByCodeUseCase,
    private val createOrderUseCase: CreateOrderUseCase,
    private val stringProvider: StringProvider,
    private val amountFormatter: Formatter<Double>,
    private val amountParser: StringParser<Double>
) : ViewModel() {

    private val _initialLoadingData = MutableLiveData<LoadingData<Unit>>()
    val initialLoadingData: LiveData<LoadingData<Unit>> = _initialLoadingData

    private val _createTradeLoadingData = MutableLiveData<LoadingData<Int>>()
    val createTradeLoadingData: LiveData<LoadingData<Int>> = _createTradeLoadingData

    private val _fiatAmount = MutableLiveData<Double>(0.0)
    val fiatAmount: LiveData<Double> = _fiatAmount

    private val _fiatAmountError = MutableLiveData<String?>()
    val fiatAmountError: LiveData<String?> = _fiatAmountError

    private val _coin = MutableLiveData<LocalCoinType>()
    val coin: LiveData<LocalCoinType> = _coin

    val cryptoAmount: LiveData<TradeCryptoAmount> = DoubleCombinedLiveData(fiatAmount, coin) { fiat, coin ->
        TradeCryptoAmount(fiat?.div(trade?.price ?: 0.0) ?: 0.0, coin?.name.orEmpty())
    }
    val platformFee: LiveData<TradeFee> = DoubleCombinedLiveData(cryptoAmount, coin) { crypto, coin ->
        TradeFee(
            platformFeePercent,
            (crypto?.cryptoAmount ?: 0.0) * platformFeePercent / 100,
            coin?.name.orEmpty()
        )
    }

    private var trade: TradeItem? = null
    private var platformFeePercent: Double = 0.0
    private var reservedBalanceUsd: Double = 0.0

    fun formatAmount(amount: Double) = amountFormatter.format(amount)

    fun parseAmount(input: String) = amountParser.parse(input)

    fun updateAmount(amount: Double) {
        _fiatAmount.value = amount
    }

    fun fetchTradeDetails(tradeId: Int) {
        _initialLoadingData.value = LoadingData.Loading()
        getTradeDetailsUseCase(tradeId, onSuccess = { trade ->
            getCoinDetailsUseCase(
                params = GetCoinDetailsUseCase.Params(trade.coin.name),
                onSuccess = { coinDetails ->
                    getCoinByCodeUseCase(trade.coin.name, onSuccess = { coinDataItem ->
                        this.trade = trade
                        platformFeePercent = coinDetails.platformTradeFee
                        _coin.value = trade.coin
                        _initialLoadingData.value = LoadingData.Success(Unit)
                        reservedBalanceUsd = coinDataItem.reservedBalanceUsd
                    }, onError = { _initialLoadingData.value = LoadingData.Error(it) })
                }, onError = { _initialLoadingData.value = LoadingData.Error(it) })
        }, onError = { _initialLoadingData.value = LoadingData.Error(it) })
    }


    fun createOrder() {
        val tradeData = trade ?: return
        val amount = fiatAmount.value ?: 0.0
        val takerActionType = if (tradeData.tradeType == TradeType.BUY) TradeType.SELL else TradeType.BUY
        val tradeAmountRange = tradeData.minLimit..tradeData.maxLimit
        if (takerActionType == TradeType.BUY && amount !in tradeAmountRange) {
            _fiatAmountError.value = stringProvider.getString(R.string.trade_buy_sell_dialog_amount_not_in_range_error)
            return
        }
        if (takerActionType == TradeType.SELL && amount > reservedBalanceUsd) {
            _fiatAmountError.value = stringProvider.getString(R.string.trade_buy_sell_dialog_amount_too_big_error)
            return
        }
        _fiatAmountError.value = null
        _createTradeLoadingData.value = LoadingData.Loading()
        createOrderUseCase(TradeOrderItem(
            tradeData.tradeId, tradeData.price,
            cryptoAmount.value?.cryptoAmount ?: 0.0,
            fiatAmount.value ?: 0.0,
            tradeData.terms
        ), onSuccess = {
            _createTradeLoadingData.value = LoadingData.Success(it)
        }, onError = {
            _createTradeLoadingData.value = LoadingData.Error(it)
        })
    }

}