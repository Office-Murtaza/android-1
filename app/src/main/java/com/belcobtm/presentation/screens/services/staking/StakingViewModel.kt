package com.belcobtm.presentation.screens.services.staking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belcobtm.R
import com.belcobtm.domain.Failure
import com.belcobtm.domain.service.ServiceInfoProvider
import com.belcobtm.domain.service.ServiceType
import com.belcobtm.domain.transaction.interactor.GetTransactionPlanUseCase
import com.belcobtm.domain.transaction.interactor.StakeCancelUseCase
import com.belcobtm.domain.transaction.interactor.StakeCreateUseCase
import com.belcobtm.domain.transaction.interactor.StakeDetailsGetUseCase
import com.belcobtm.domain.transaction.interactor.StakeWithdrawUseCase
import com.belcobtm.domain.transaction.item.StakeDetailsDataItem
import com.belcobtm.domain.transaction.item.TransactionPlanItem
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.domain.wallet.interactor.GetCoinByCodeUseCase
import com.belcobtm.domain.wallet.item.CoinDataItem
import com.belcobtm.presentation.core.DateFormat
import com.belcobtm.presentation.core.SingleLiveData
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.provider.string.StringProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StakingViewModel(
    private val getCoinByCodeUseCase: GetCoinByCodeUseCase,
    private val stakeCreateUseCase: StakeCreateUseCase,
    private val stakeCancelUseCase: StakeCancelUseCase,
    private val stakeWithdrawUseCase: StakeWithdrawUseCase,
    private val stakeDetailsUseCase: StakeDetailsGetUseCase,
    private val getTransactionPlanUseCase: GetTransactionPlanUseCase,
    private val serviceInfoProvider: ServiceInfoProvider,
    private val stringProvider: StringProvider,
) : ViewModel() {

    private var stakeDetailsDataItem: StakeDetailsDataItem? = null
    private var transactionPlanItem: TransactionPlanItem? = null

    private val _stakeDetailsLiveData = MutableLiveData<LoadingData<StakingScreenItem>>()
    val stakeDetailsLiveData: LiveData<LoadingData<StakingScreenItem>> = _stakeDetailsLiveData

    private val _transactionLiveData = SingleLiveData<LoadingData<StakingTransactionState>>()
    val transactionLiveData: LiveData<LoadingData<StakingTransactionState>> = _transactionLiveData

    private lateinit var coinDataItem: CoinDataItem
    private lateinit var etheriumCoinDataItem: CoinDataItem

    init {
        loadData()
    }

    fun loadData() {
        val catmCoinCode = LocalCoinType.CATM.name
        _stakeDetailsLiveData.value = LoadingData.Loading()
        getTransactionPlanUseCase(
            catmCoinCode,
            onSuccess = { planItem ->
                transactionPlanItem = planItem
                getCoinByCodeUseCase(
                    catmCoinCode,
                    onSuccess = { catmCoin ->
                        this.coinDataItem = catmCoin
                        // it is necessary to get latest data as we will be checking
                        // balance value to proceess next operations
                        getCoinByCodeUseCase(
                            LocalCoinType.ETH.name,
                            onSuccess = { etherium ->
                                etheriumCoinDataItem = etherium
                                loadBaseData(planItem)
                            },
                            onError = { failure2 ->
                                _stakeDetailsLiveData.value = LoadingData.Error(failure2)
                            }
                        )
                    },
                    onError = {
                        _stakeDetailsLiveData.value = LoadingData.Error(it)
                    }
                )
            },
            onError = {
                _stakeDetailsLiveData.value = LoadingData.Error(it)
            }
        )
    }

    fun showLocationError() {
        _transactionLiveData.value = LoadingData.Error(
            Failure.LocationError(
                stringProvider.getString(R.string.location_required_on_trade_creation)
            )
        )
    }

    private fun loadBaseData(planItem: TransactionPlanItem) {
        stakeDetailsUseCase.invoke(
            params = StakeDetailsGetUseCase.Params(coinDataItem.code),
            onError = { _stakeDetailsLiveData.value = LoadingData.Error(it) },
            onSuccess = { stakeDataItem ->
                stakeDetailsDataItem = stakeDataItem
                _stakeDetailsLiveData.value = LoadingData.Success(
                    StakingScreenItem(
                        price = coinDataItem.priceUsd,
                        balanceCoin = coinDataItem.balanceCoin,
                        balanceUsd = coinDataItem.balanceUsd,
                        ethFee = planItem.txFee,
                        reservedBalanceCoin = coinDataItem.reservedBalanceCoin,
                        reservedBalanceUsd = coinDataItem.reservedBalanceUsd,
                        reservedCode = coinDataItem.code,
                        amount = stakeDataItem.amount,
                        status = stakeDataItem.status,
                        rewardsAmount = stakeDataItem.rewardsAmount,
                        rewardsPercent = stakeDataItem.rewardsPercent,
                        rewardsAmountAnnual = stakeDataItem.rewardsAnnualAmount,
                        rewardsPercentAnnual = stakeDataItem.rewardsAnnualPercent,
                        createDate = convertToStringRepresentation(stakeDataItem.createTimestamp),
                        cancelDate = convertToStringRepresentation(stakeDataItem.cancelTimestamp),
                        duration = stakeDataItem.duration,
                        cancelHoldPeriod = stakeDataItem.cancelHoldPeriod,
                        untilWithdraw = stakeDataItem.untilWithdraw
                    )
                )
            },
        )
    }

    fun stakeCreate(amount: Double) = viewModelScope.launch {
        val transactionPlanItem = transactionPlanItem ?: return@launch
        val feePercent = serviceInfoProvider.getService(ServiceType.STAKING)?.feePercent ?: 0.0
        val usdAmount = amount * getUsdPrice()
        val service = serviceInfoProvider.getService(ServiceType.STAKING)
        if (service == null || service.txLimit < usdAmount || service.remainLimit < usdAmount) {
            _transactionLiveData.value = LoadingData.Error(
                Failure.MessageError(
                    stringProvider.getString(R.string.limits_exceeded_validation_message)
                )
            )
            return@launch
        }
        _transactionLiveData.value = LoadingData.Loading()
        stakeCreateUseCase.invoke(
            params = StakeCreateUseCase.Params(
                coinDataItem.code, amount, feePercent,
                usdAmount, transactionPlanItem,
            ),
            onSuccess = {
                viewModelScope.launch {
                    delay(1000L)
                    loadData()
                    _transactionLiveData.value =
                        LoadingData.Success(StakingTransactionState.CREATE)
                }
            },
            onError = {
                _transactionLiveData.value = LoadingData.Error(it, StakingTransactionState.CREATE)
            }
        )
    }

    fun stakeCancel() {
        val transactionPlanItem = transactionPlanItem ?: return
        _transactionLiveData.value = LoadingData.Loading()
        stakeCancelUseCase.invoke(
            params = StakeCancelUseCase.Params(coinDataItem.code, transactionPlanItem),
            onSuccess = {
                viewModelScope.launch {
                    delay(1000L)
                    loadData()
                    _transactionLiveData.value = LoadingData.Success(StakingTransactionState.CANCEL)
                }
            },
            onError = {
                _transactionLiveData.value = LoadingData.Error(it, StakingTransactionState.CANCEL)
            }
        )
    }

    fun unstakeCreateTransaction() = viewModelScope.launch {
        val amount =
            (stakeDetailsDataItem?.amount ?: 0.0) + (stakeDetailsDataItem?.rewardsAmount ?: 0.0)
        val transactionPlanItem = transactionPlanItem ?: return@launch
        _transactionLiveData.value = LoadingData.Loading()
        val usdAmount = amount * getUsdPrice()
        val service = serviceInfoProvider.getService(ServiceType.STAKING)
        if (service == null || service.txLimit < usdAmount || service.remainLimit < usdAmount) {
            _transactionLiveData.value = LoadingData.Error(
                Failure.MessageError(
                    stringProvider.getString(R.string.limits_exceeded_validation_message)
                )
            )
            return@launch
        }
        stakeWithdrawUseCase.invoke(
            params = StakeWithdrawUseCase.Params(coinDataItem.code, amount, transactionPlanItem),
            onSuccess = {
                viewModelScope.launch {
                    delay(1000L)
                    loadData()
                    _transactionLiveData.value =
                        LoadingData.Success(StakingTransactionState.WITHDRAW)
                }
            },
            onError = {
                _transactionLiveData.value =
                    LoadingData.Error(it, StakingTransactionState.WITHDRAW)
            }
        )
    }

    fun isNotEnoughETHBalanceForCATM(): Boolean =
        etheriumCoinDataItem.balanceCoin < (transactionPlanItem?.txFee ?: 0.0)

    fun getMaxValue(): Double = if (coinDataItem.code == LocalCoinType.CATM.name) {
        coinDataItem.balanceCoin
    } else {
        0.0.coerceAtLeast(coinDataItem.balanceCoin - (transactionPlanItem?.txFee ?: 0.0))
    }

    fun getUsdPrice(): Double = coinDataItem.priceUsd

    private fun convertToStringRepresentation(timestamp: Long?): String? {
        if (timestamp == null) {
            return null
        }
        return DateFormat.sdfShort.format(timestamp)
    }

}
