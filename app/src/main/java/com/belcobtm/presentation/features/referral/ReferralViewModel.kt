package com.belcobtm.presentation.features.referral

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.belcobtm.domain.referral.LoadReferralUseCase
import com.belcobtm.domain.referral.item.ReferralDataItem
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.domain.wallet.interactor.GetCoinByCodeUseCase
import com.belcobtm.presentation.core.mvvm.LoadingData

class ReferralViewModel(
    private val loadReferralUseCase: LoadReferralUseCase,
    private val getCoinByCodeUseCase: GetCoinByCodeUseCase
) : ViewModel() {

    companion object {
        const val REFERRAL_REWARD_COIN_AMOUNT = 100
    }

    private val _initialLoadingData = MutableLiveData<LoadingData<Pair<ReferralDataItem, Double>>>()
    val initialLoadingData: LiveData<LoadingData<Pair<ReferralDataItem, Double>>> =
        _initialLoadingData

    private val _referralUsdRewardAmount = MutableLiveData<Double>()
    val referralUsdRewardAmount: LiveData<Double> = _referralUsdRewardAmount

    fun loadData() {
        _initialLoadingData.value = LoadingData.Loading()
        loadReferralUseCase.invoke(Unit, onSuccess = { referralDataItem ->
            getCoinByCodeUseCase(LocalCoinType.CATM.name, onSuccess = {
                _referralUsdRewardAmount.value = it.priceUsd * REFERRAL_REWARD_COIN_AMOUNT
                _initialLoadingData.value = LoadingData.Success(
                    referralDataItem to it.priceUsd * referralDataItem.earned
                )
            }, onError = {
                _initialLoadingData.value = LoadingData.Error(it)
            })
        }, onError = {
            _initialLoadingData.value = LoadingData.Error(it)
        })
    }

    fun getReferralMessage(): String =
        (initialLoadingData.value as? LoadingData.Success<Pair<ReferralDataItem, Double>>)?.data
            ?.first?.message.orEmpty()

    fun getReferralLink(): String =
        (initialLoadingData.value as? LoadingData.Success<Pair<ReferralDataItem, Double>>)?.data
            ?.first?.link.orEmpty()
}