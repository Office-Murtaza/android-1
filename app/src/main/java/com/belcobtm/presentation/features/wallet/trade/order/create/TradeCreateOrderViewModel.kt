package com.belcobtm.presentation.features.wallet.trade.order.create

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.belcobtm.R
import com.belcobtm.data.disk.database.service.ServiceType
import com.belcobtm.data.model.trade.TradeType
import com.belcobtm.domain.Failure
import com.belcobtm.domain.service.ServiceInfoProvider
import com.belcobtm.domain.trade.details.GetTradeDetailsUseCase
import com.belcobtm.domain.trade.order.CreateOrderUseCase
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.domain.wallet.interactor.GetCoinByCodeUseCase
import com.belcobtm.presentation.core.extensions.toStringCoin
import com.belcobtm.presentation.core.formatter.Formatter
import com.belcobtm.presentation.core.livedata.DoubleCombinedLiveData
import com.belcobtm.presentation.core.livedata.TripleCombinedLiveData
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.provider.string.StringProvider
import com.belcobtm.presentation.features.wallet.trade.list.model.TradeItem
import com.belcobtm.presentation.features.wallet.trade.order.create.model.*

class TradeCreateOrderViewModel(
    private val getTradeDetailsUseCase: GetTradeDetailsUseCase,
    private val getCoinByCodeUseCase: GetCoinByCodeUseCase,
    private val createOrderUseCase: CreateOrderUseCase,
    private val stringProvider: StringProvider,
    private val serviceInfoProvider: ServiceInfoProvider,
    private val priceFormatter: Formatter<Double>
) : ViewModel() {

    private val _initialLoadingData = MutableLiveData<LoadingData<Unit>>()
    val initialLoadingData: LiveData<LoadingData<Unit>> = _initialLoadingData

    private val _createTradeLoadingData = MutableLiveData<LoadingData<String>>()
    val createTradeLoadingData: LiveData<LoadingData<String>> = _createTradeLoadingData

    private val _fiatAmount = MutableLiveData<Double>(0.0)
    val fiatAmount: LiveData<Double> = _fiatAmount

    private val _fiatAmountError = MutableLiveData<String?>()
    val fiatAmountError: LiveData<String?> = _fiatAmountError

    private val _coin = MutableLiveData<LocalCoinType>()
    val coin: LiveData<LocalCoinType> = _coin

    private val _reservedBalance = MutableLiveData<ReservedBalance>()
    val reservedBalance: LiveData<ReservedBalance> = _reservedBalance

    private val _receiveAmountLabel = MutableLiveData<Int>()

    private var includeFeeCoef = 1

    val cryptoAmount: LiveData<TradeCryptoAmount> =
        DoubleCombinedLiveData(fiatAmount, coin) { fiat, coin ->
            TradeCryptoAmount(fiat?.div(trade?.price ?: 0.0) ?: 0.0, coin?.name.orEmpty())
        }
    val platformFee: LiveData<TradeFee> =
        DoubleCombinedLiveData(cryptoAmount, coin) { crypto, coin ->
            TradeFee(
                platformFeePercent,
                (crypto?.cryptoAmount ?: 0.0) * platformFeePercent / 2 / 100,
                coin?.name.orEmpty()
            )
        }

    val amountWithoutFee: LiveData<TotalValue> =
        TripleCombinedLiveData(
            cryptoAmount,
            platformFee,
            _receiveAmountLabel
        ) { amount, fee, labelId ->
            TotalValue(
                (amount?.cryptoAmount ?: 0.0) + includeFeeCoef * (fee?.platformFeeCrypto ?: 0.0),
                fee?.coinCode.orEmpty(),
                labelId ?: R.string.you_will_get_label
            )
        }

    private var trade: TradeItem? = null
    private var platformFeePercent: Double = 0.0
    private var reservedBalanceUsd: Double = 0.0

    fun updateAmount(amount: Double) {
        _fiatAmount.value = amount
    }

    fun fetchTradeDetails(tradeId: String) {
        _initialLoadingData.value = LoadingData.Loading()
        getTradeDetailsUseCase(tradeId, onSuccess = { trade ->
            getCoinByCodeUseCase(trade.coin.name, onSuccess = { coinDataItem ->
                this.trade = trade
                this.platformFeePercent = serviceInfoProvider.getServiceFee(ServiceType.TRADE)
                this._coin.value = trade.coin
                this.reservedBalanceUsd = coinDataItem.reservedBalanceUsd
                _reservedBalance.value = ReservedBalance(
                    coinDataItem.reservedBalanceCoin,
                    priceFormatter.format(coinDataItem.reservedBalanceUsd),
                    coinDataItem.code
                )
                if (trade.tradeType == TradeType.BUY) {
                    includeFeeCoef = 1
                    _receiveAmountLabel.value = R.string.you_will_send_label
                } else {
                    includeFeeCoef = -1
                    _receiveAmountLabel.value = R.string.you_will_get_label
                }
                this._initialLoadingData.value = LoadingData.Success(Unit)
            }, onError = { _initialLoadingData.value = LoadingData.Error(it) })
        }, onError = { _initialLoadingData.value = LoadingData.Error(it) })
    }


    fun createOrder() {
        val tradeData = trade ?: return
        val amount = fiatAmount.value ?: 0.0
        val takerActionType =
            if (tradeData.tradeType == TradeType.BUY) TradeType.SELL else TradeType.BUY
        val tradeAmountRange = tradeData.minLimit..tradeData.maxLimit
        if (amount == 0.0) {
            _fiatAmountError.value =
                stringProvider.getString(R.string.trade_buy_sell_dialog_amount_zero_error)
            return
        }
        if (takerActionType == TradeType.BUY && amount !in tradeAmountRange) {
            _fiatAmountError.value =
                stringProvider.getString(R.string.trade_buy_sell_dialog_amount_not_in_range_error)
            return
        }
        if (takerActionType == TradeType.SELL && amount > reservedBalanceUsd) {
            _fiatAmountError.value =
                stringProvider.getString(R.string.trade_buy_sell_dialog_amount_too_big_error)
            return
        }
        _fiatAmountError.value = null
        _createTradeLoadingData.value = LoadingData.Loading()
        createOrderUseCase(TradeOrderItem(
            tradeData.tradeId, tradeData.price,
            cryptoAmount.value?.cryptoAmount?.toStringCoin()?.toDouble() ?: 0.0,
            fiatAmount.value ?: 0.0
        ), onSuccess = {
            _createTradeLoadingData.value = LoadingData.Success(it)
        }, onError = {
            if (it is Failure.ValidationError) {
                _fiatAmountError.value = it.message
            }
            _createTradeLoadingData.value = LoadingData.Error(it)
        })
    }

}