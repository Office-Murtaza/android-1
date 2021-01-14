package com.app.belcobtm.presentation.features.deals.staking

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.belcobtm.data.websockets.wallet.WalletObserver
import com.app.belcobtm.data.websockets.wallet.model.WalletBalance
import com.app.belcobtm.domain.transaction.interactor.StakeCancelUseCase
import com.app.belcobtm.domain.transaction.interactor.StakeCreateUseCase
import com.app.belcobtm.domain.transaction.interactor.StakeDetailsGetUseCase
import com.app.belcobtm.domain.transaction.interactor.StakeWithdrawUseCase
import com.app.belcobtm.domain.transaction.item.StakeDetailsDataItem
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.domain.wallet.interactor.GetCoinByCodeUseCase
import com.app.belcobtm.domain.wallet.interactor.GetCoinDetailsUseCase
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.domain.wallet.item.CoinDetailsDataItem
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class StakingViewModel(
    private val walletObserver: WalletObserver,
    private val getCoinDetailsUseCase: GetCoinDetailsUseCase,
    private val getCoinByCodeUseCase: GetCoinByCodeUseCase,
    private val stakeCreateUseCase: StakeCreateUseCase,
    private val stakeCancelUseCase: StakeCancelUseCase,
    private val stakeWithdrawUseCase: StakeWithdrawUseCase,
    private val stakeDetailsUseCase: StakeDetailsGetUseCase
) : ViewModel() {
    private var stakeDetailsDataItem: StakeDetailsDataItem? = null
    val stakeDetailsLiveData: MutableLiveData<LoadingData<StakingScreenItem>> = MutableLiveData()
    val transactionLiveData: MutableLiveData<LoadingData<StakingTransactionState>> =
        MutableLiveData()

    private lateinit var coinDataItem: CoinDataItem
    private lateinit var coinDetailsDataItem: CoinDetailsDataItem

    init {
        loadData()
    }

    fun loadData() {
        stakeDetailsLiveData.value = LoadingData.Loading()
        viewModelScope.launch {
            walletObserver.observe()
                .receiveAsFlow()
                .filterIsInstance<WalletBalance.Balance>()
                .mapNotNull { balance -> balance.data.coinList.find { it.code == LocalCoinType.CATM.name } }
                .collect { catmCoin ->
                    coinDataItem = catmCoin
                    getCoinDetailsUseCase.invoke(
                        GetCoinDetailsUseCase.Params(LocalCoinType.CATM.name),
                        onSuccess = {
                            coinDetailsDataItem = it
                            loadBaseData()
                        },
                        onError = {
                            stakeDetailsLiveData.value = LoadingData.Error(it)
                        }
                    )
                }
        }
    }

    private fun loadBaseData() {
        stakeDetailsUseCase.invoke(
            params = StakeDetailsGetUseCase.Params(coinDataItem.code),
            onError = { stakeDetailsLiveData.value = LoadingData.Error(it) },
            onSuccess = { stakeDataItem ->
                stakeDetailsDataItem = stakeDataItem
                stakeDetailsLiveData.value = LoadingData.Success(
                    StakingScreenItem(
                        price = coinDataItem.priceUsd,
                        balanceCoin = coinDataItem.balanceCoin,
                        balanceUsd = coinDataItem.balanceUsd,
                        ethFee = coinDetailsDataItem.txFee,
                        reservedBalanceCoin = coinDataItem.reservedBalanceCoin,
                        reservedBalanceUsd = coinDataItem.reservedBalanceUsd,
                        reservedCode = coinDataItem.code,
                        amount = stakeDataItem.amount,
                        status = stakeDataItem.status,
                        rewardsAmount = stakeDataItem.rewardsAmount,
                        rewardsPercent = stakeDataItem.rewardsPercent,
                        rewardsAmountAnnual = stakeDataItem.rewardsAnnualAmount,
                        rewardsPercentAnnual = stakeDataItem.rewardsAnnualPercent,
                        createDate = stakeDataItem.createDate,
                        cancelDate = stakeDataItem.cancelDate,
                        duration = stakeDataItem.duration,
                        cancelHoldPeriod = stakeDataItem.cancelHoldPeriod,
                        untilWithdraw = stakeDataItem.untilWithdraw
                    )
                )
            }
        )
    }

    fun stakeCreate(amount: Double) {
        transactionLiveData.value = LoadingData.Loading()
        stakeCreateUseCase.invoke(
            params = StakeCreateUseCase.Params(coinDataItem.code, amount),
            onSuccess = {
                transactionLiveData.value = LoadingData.Success(StakingTransactionState.CREATE)
            },
            onError = {
                transactionLiveData.value = LoadingData.Error(it, StakingTransactionState.CREATE)
            }
        )
    }

    fun stakeCancel() {
        transactionLiveData.value = LoadingData.Loading()
        stakeCancelUseCase.invoke(
            params = StakeCancelUseCase.Params(coinDataItem.code),
            onSuccess = {
                transactionLiveData.value = LoadingData.Success(StakingTransactionState.CANCEL)
            },
            onError = {
                transactionLiveData.value = LoadingData.Error(it, StakingTransactionState.CANCEL)
            }
        )
    }

    fun unstakeCreateTransaction() {
        val amount =
            (stakeDetailsDataItem?.amount ?: 0.0) + (stakeDetailsDataItem?.rewardsAmount ?: 0.0)
        transactionLiveData.value = LoadingData.Loading()
        stakeWithdrawUseCase.invoke(
            params = StakeWithdrawUseCase.Params(coinDataItem.code, amount),
            onSuccess = {
                transactionLiveData.value = LoadingData.Success(StakingTransactionState.WITHDRAW)
            },
            onError = {
                transactionLiveData.value = LoadingData.Error(it, StakingTransactionState.WITHDRAW)
            }
        )
    }

    fun isNotEnoughETHBalanceForCATM(): Boolean =
        getCoinByCodeUseCase.invoke(LocalCoinType.ETH.name).balanceCoin < coinDetailsDataItem.txFee

    fun getMaxValue(): Double = if (coinDataItem.code == LocalCoinType.CATM.name) {
        coinDataItem.balanceCoin
    } else {
        0.0.coerceAtLeast(coinDataItem.balanceCoin - coinDetailsDataItem.txFee)
    }

    fun getUsdPrice(): Double = coinDataItem.priceUsd
}