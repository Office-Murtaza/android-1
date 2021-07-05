package com.belcobtm.presentation.features.wallet.add

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.belcobtm.domain.account.interactor.UpdateUserCoinListUseCase
import com.belcobtm.domain.account.interactor.GetUserCoinListUseCase
import com.belcobtm.domain.wallet.item.AccountDataItem
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.features.wallet.add.adapter.AddWalletCoinItem

class WalletsViewModel(
    coinListUseCase: GetUserCoinListUseCase,
    private val updateUserCoinListUseCase: UpdateUserCoinListUseCase
) : ViewModel() {
    val coinListLiveData: MutableLiveData<LoadingData<List<AddWalletCoinItem>>> = MutableLiveData()
    private val accountDataList: MutableList<AccountDataItem> = mutableListOf()

    private var lastAction = {}

    init {
        coinListUseCase.invoke { result ->
            accountDataList.addAll(result)
            updateCoinList()
        }
    }

    fun retry() {
        lastAction()
    }

    fun changeCoinState(position: Int, isChecked: Boolean) {
        lastAction = {
            coinListLiveData.value = LoadingData.Loading()
            val coinDataItem = accountDataList[position]
            coinDataItem.isEnabled = isChecked
            updateUserCoinListUseCase.invoke(
                UpdateUserCoinListUseCase.Params(coinDataItem),
                onSuccess = {
                    updateCoinList()
                },
                onError = {
                    coinListLiveData.value = LoadingData.Error(it)
                }
            )
        }
        lastAction()
    }


    private fun updateCoinList() {
        coinListLiveData.value = LoadingData.Success(accountDataList.map {
            AddWalletCoinItem(
                it.type.name,
                it.isEnabled
            )
        })
    }

}