package com.app.belcobtm.presentation.features.wallet.add

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.transaction.interactor.UpdateCoinUseCase
import com.app.belcobtm.domain.wallet.interactor.GetLocalCoinListUseCase
import com.app.belcobtm.domain.wallet.item.AccountDataItem
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.features.wallet.add.adapter.AddWalletCoinItem

class WalletsViewModel(
    coinListUseCase: GetLocalCoinListUseCase,
    private val updateCoinUseCase: UpdateCoinUseCase
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
            updateCoinUseCase.invoke(UpdateCoinUseCase.Params(coinDataItem),
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