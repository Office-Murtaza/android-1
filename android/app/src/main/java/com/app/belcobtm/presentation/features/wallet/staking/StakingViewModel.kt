package com.app.belcobtm.presentation.features.wallet.staking

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.transaction.interactor.StakeCancelUseCase
import com.app.belcobtm.domain.transaction.interactor.StakeCreateUseCase
import com.app.belcobtm.domain.transaction.interactor.StakeDetailsGetUseCase
import com.app.belcobtm.domain.transaction.interactor.StakeWithdrawUseCase
import com.app.belcobtm.domain.transaction.item.StakeDetailsDataItem
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.domain.wallet.interactor.GetCoinByCodeUseCase
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.domain.wallet.item.CoinDetailsDataItem
import com.app.belcobtm.presentation.core.mvvm.LoadingData

class StakingViewModel(
    private val coinDataItem: CoinDataItem,
    private val coinDetailsDataItem: CoinDetailsDataItem,
    private val getCoinByCodeUseCase: GetCoinByCodeUseCase,
    private val stakeCreateUseCase: StakeCreateUseCase,
    private val stakeCancelUseCase: StakeCancelUseCase,
    private val stakeWithdrawUseCase: StakeWithdrawUseCase,
    private val stakeDetailsUseCase: StakeDetailsGetUseCase
) : ViewModel() {
    private var stakeDetailsDataItem: StakeDetailsDataItem? = null
    val stakeDetailsLiveData: MutableLiveData<LoadingData<StakingScreenItem>> = MutableLiveData()
    val transactionLiveData: MutableLiveData<LoadingData<StakingTransactionState>> = MutableLiveData()

    init {
        loadBaseData()
    }

    fun loadBaseData() {
        stakeDetailsLiveData.value = LoadingData.Loading()
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
                        holdPeriod = stakeDataItem.holdPeriod,
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
            onSuccess = { transactionLiveData.value = LoadingData.Success(StakingTransactionState.CREATE) },
            onError = { transactionLiveData.value = LoadingData.Error(it, StakingTransactionState.CREATE) }
        )
    }

    fun stakeCancel() {
        transactionLiveData.value = LoadingData.Loading()
        stakeCancelUseCase.invoke(
            params = StakeCancelUseCase.Params(coinDataItem.code),
            onSuccess = { transactionLiveData.value = LoadingData.Success(StakingTransactionState.CANCEL) },
            onError = { transactionLiveData.value = LoadingData.Error(it, StakingTransactionState.CANCEL) }
        )
    }

    fun unstakeCreateTransaction() {
        val amount = (stakeDetailsDataItem?.amount ?: 0.0) + (stakeDetailsDataItem?.rewardsAmount ?: 0.0)
        transactionLiveData.value = LoadingData.Loading()
        stakeWithdrawUseCase.invoke(
            params = StakeWithdrawUseCase.Params(coinDataItem.code, amount),
            onSuccess = { transactionLiveData.value = LoadingData.Success(StakingTransactionState.WITHDRAW) },
            onError = { transactionLiveData.value = LoadingData.Error(it, StakingTransactionState.WITHDRAW) }
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