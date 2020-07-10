package com.app.belcobtm.presentation.features.wallet.staking

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.transaction.interactor.*
import com.app.belcobtm.domain.transaction.item.StakeDetailsDataItem
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.domain.wallet.interactor.GetCoinByCodeUseCase
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.domain.wallet.item.CoinFeeDataItem
import com.app.belcobtm.presentation.core.mvvm.LoadingData

class StakingViewModel(
    private val coinDataItem: CoinDataItem,
    private val coinFeeDataItem: CoinFeeDataItem,
    private val getCoinByCodeUseCase: GetCoinByCodeUseCase,
    private val stakeCreateTransactionUseCase: StakeCreateTransactionUseCase,
    private val stakeCompleteTransactionUseCase: StakeCompleteTransactionUseCase,
    private val unStakeCreateTransactionUseCase: UnStakeCreateTransactionUseCase,
    private val unStakeCompleteTransactionUseCase: UnStakeCompleteTransactionUseCase,
    stakeDetailsUseCase: StakeDetailsGetUseCase
) : ViewModel() {
    private var hash: String = ""
    private var stakeDetailsDataItem: StakeDetailsDataItem? = null
    val stakeDetailsLiveData: MutableLiveData<LoadingData<StakingScreenItem>> = MutableLiveData()
    val transactionLiveData: MutableLiveData<LoadingData<TransactionState>> = MutableLiveData()

    init {
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
                        isExist = stakeDataItem.exist,
                        isUnStakeAvailable = stakeDataItem.isUnStakeAvailable,
                        staked = stakeDataItem.stakedAmount,
                        rewardsAmount = stakeDataItem.rewardsAmount,
                        rewardsPercent = stakeDataItem.rewardsPercent,
                        time = stakeDataItem.stakedDays
                    )
                )
            }
        )
    }

    fun stakeCreateTransaction(amount: Double) {
        transactionLiveData.value = LoadingData.Loading()
        stakeCreateTransactionUseCase.invoke(
            params = StakeCreateTransactionUseCase.Params(coinDataItem.code, amount),
            onSuccess = {
                hash = it
                transactionLiveData.value = LoadingData.Success(TransactionState.STAKE_CREATE)
            },
            onError = { transactionLiveData.value = LoadingData.Error(it, TransactionState.STAKE_CREATE) }
        )
    }

    fun stakeCompleteTransaction(smsCode: String) {
        val amount = (stakeDetailsDataItem?.stakedAmount ?: 0.0) + (stakeDetailsDataItem?.rewardsAmount ?: 0.0)
        transactionLiveData.value = LoadingData.Loading()
        stakeCompleteTransactionUseCase.invoke(
            params = StakeCompleteTransactionUseCase.Params(smsCode, hash, coinDataItem.code, amount),
            onSuccess = { transactionLiveData.value = LoadingData.Success(TransactionState.STAKE_COMPLETE) },
            onError = { transactionLiveData.value = LoadingData.Error(it, TransactionState.STAKE_COMPLETE) }
        )
    }

    fun unstakeCreateTransaction() {
        val amount = (stakeDetailsDataItem?.stakedAmount ?: 0.0) + (stakeDetailsDataItem?.rewardsAmount ?: 0.0)
        transactionLiveData.value = LoadingData.Loading()
        unStakeCreateTransactionUseCase.invoke(
            params = UnStakeCreateTransactionUseCase.Params(coinDataItem.code, amount),
            onSuccess = {
                hash = it
                transactionLiveData.value = LoadingData.Success(TransactionState.UNSTAKE_CREATE)
            },
            onError = { transactionLiveData.value = LoadingData.Error(it, TransactionState.UNSTAKE_CREATE) }
        )
    }

    fun unstakeCompleteTransaction(smsCode: String, amount: Double) {
        transactionLiveData.value = LoadingData.Loading()
        unStakeCompleteTransactionUseCase.invoke(
            params = UnStakeCompleteTransactionUseCase.Params(smsCode, hash, coinDataItem.code, amount),
            onSuccess = { transactionLiveData.value = LoadingData.Success(TransactionState.UNSTAKE_COMPLETE) },
            onError = { transactionLiveData.value = LoadingData.Error(it, TransactionState.UNSTAKE_COMPLETE) }
        )
    }

    fun isNotEnoughETHBalanceForCATM(): Boolean =
        getCoinByCodeUseCase.invoke(LocalCoinType.ETH.name).balanceCoin < coinFeeDataItem.txFee

    fun getMaxValue(): Double = if (coinDataItem.code == LocalCoinType.CATM.name) {
        coinDataItem.balanceCoin
    } else {
        0.0.coerceAtLeast(coinDataItem.balanceCoin - coinFeeDataItem.txFee)
    }

    enum class TransactionState {
        STAKE_CREATE, STAKE_COMPLETE, UNSTAKE_CREATE, UNSTAKE_COMPLETE
    }
}