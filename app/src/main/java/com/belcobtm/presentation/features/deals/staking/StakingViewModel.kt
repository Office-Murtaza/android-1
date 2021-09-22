package com.belcobtm.presentation.features.deals.staking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belcobtm.domain.transaction.interactor.StakeCancelUseCase
import com.belcobtm.domain.transaction.interactor.StakeCreateUseCase
import com.belcobtm.domain.transaction.interactor.StakeDetailsGetUseCase
import com.belcobtm.domain.transaction.interactor.StakeWithdrawUseCase
import com.belcobtm.domain.transaction.item.StakeDetailsDataItem
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.domain.wallet.interactor.GetCoinByCodeUseCase
import com.belcobtm.domain.wallet.item.CoinDataItem
import com.belcobtm.presentation.core.DateFormat
import com.belcobtm.presentation.core.SingleLiveData
import com.belcobtm.presentation.core.mvvm.LoadingData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StakingViewModel(
    private val getCoinByCodeUseCase: GetCoinByCodeUseCase,
    private val stakeCreateUseCase: StakeCreateUseCase,
    private val stakeCancelUseCase: StakeCancelUseCase,
    private val stakeWithdrawUseCase: StakeWithdrawUseCase,
    private val stakeDetailsUseCase: StakeDetailsGetUseCase
) : ViewModel() {
    private var stakeDetailsDataItem: StakeDetailsDataItem? = null

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
                        loadBaseData()
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
    }

    private fun loadBaseData() {
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
                        ethFee = 0.0,
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

    fun stakeCreate(amount: Double) {
        _transactionLiveData.value = LoadingData.Loading()
        stakeCreateUseCase.invoke(
            params = StakeCreateUseCase.Params(coinDataItem.code, amount),
            onSuccess = {
                viewModelScope.launch {
                    delay(1000L)
                    loadData()
                    _transactionLiveData.value = LoadingData.Success(StakingTransactionState.CREATE)
                }
            },
            onError = {
                _transactionLiveData.value = LoadingData.Error(it, StakingTransactionState.CREATE)
            }
        )
    }

    fun stakeCancel() {
        _transactionLiveData.value = LoadingData.Loading()
        stakeCancelUseCase.invoke(
            params = StakeCancelUseCase.Params(coinDataItem.code),
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

    fun unstakeCreateTransaction() {
        val amount =
            (stakeDetailsDataItem?.amount ?: 0.0) + (stakeDetailsDataItem?.rewardsAmount ?: 0.0)
        _transactionLiveData.value = LoadingData.Loading()
        stakeWithdrawUseCase.invoke(
            params = StakeWithdrawUseCase.Params(coinDataItem.code, amount),
            onSuccess = {
                viewModelScope.launch {
                    delay(1000L)
                    loadData()
                    _transactionLiveData.value =
                        LoadingData.Success(StakingTransactionState.WITHDRAW)
                }
            },
            onError = {
                _transactionLiveData.value = LoadingData.Error(it, StakingTransactionState.WITHDRAW)
            }
        )
    }

    fun isNotEnoughETHBalanceForCATM(): Boolean =
        etheriumCoinDataItem.balanceCoin < 0.0

    fun getMaxValue(): Double = if (coinDataItem.code == LocalCoinType.CATM.name) {
        coinDataItem.balanceCoin
    } else {
        0.0.coerceAtLeast(coinDataItem.balanceCoin - 0.0)
    }

    fun getUsdPrice(): Double = coinDataItem.priceUsd

    private fun convertToStringRepresentation(timestamp: Long?): String? {
        if (timestamp == null) {
            return null
        }
        return DateFormat.sdfLong.format(timestamp)
    }
}
