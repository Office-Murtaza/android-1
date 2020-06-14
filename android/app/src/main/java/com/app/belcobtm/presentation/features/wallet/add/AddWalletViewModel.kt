package com.app.belcobtm.presentation.features.wallet.add

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.transaction.interactor.UpdateCoinUseCase
import com.app.belcobtm.domain.wallet.interactor.GetLocalCoinListUseCase
import com.app.belcobtm.domain.wallet.item.LocalCoinDataItem
import com.app.belcobtm.presentation.features.wallet.add.adapter.AddWalletCoinItem

class AddWalletViewModel(
    coinListUseCase: GetLocalCoinListUseCase,
    private val updateCoinUseCase: UpdateCoinUseCase
) : ViewModel() {
    val coinListLiveData: MutableLiveData<List<AddWalletCoinItem>> = MutableLiveData()
    private val localCoinDataList: MutableList<LocalCoinDataItem> = mutableListOf()

    init {
        coinListUseCase.invoke { result ->
            localCoinDataList.addAll(result)
            coinListLiveData.value = localCoinDataList.map {
                AddWalletCoinItem(
                    it.type.name,
                    it.isEnabled
                )
            }
        }
    }

    fun changeCoinState(position: Int, isChecked: Boolean) {
        val coinDataItem = localCoinDataList[position]
        coinDataItem.isEnabled = isChecked
        updateCoinUseCase.invoke(UpdateCoinUseCase.Params(coinDataItem))
    }


}