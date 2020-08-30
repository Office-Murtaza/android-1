package com.app.belcobtm.presentation.features.wallet.add

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.transaction.interactor.UpdateCoinUseCase
import com.app.belcobtm.domain.wallet.interactor.GetLocalCoinListUseCase
import com.app.belcobtm.domain.wallet.item.AccountDataItem
import com.app.belcobtm.presentation.features.wallet.add.adapter.AddWalletCoinItem

class ManageWalletsViewModel(
    coinListUseCase: GetLocalCoinListUseCase,
    private val updateCoinUseCase: UpdateCoinUseCase
) : ViewModel() {
    val coinListLiveData: MutableLiveData<List<AddWalletCoinItem>> = MutableLiveData()
    private val accountDataList: MutableList<AccountDataItem> = mutableListOf()

    init {
        coinListUseCase.invoke { result ->
            accountDataList.addAll(result)
            coinListLiveData.value = accountDataList.map {
                AddWalletCoinItem(
                    it.type.name,
                    it.isEnabled
                )
            }
        }
    }

    fun changeCoinState(position: Int, isChecked: Boolean) {
        val coinDataItem = accountDataList[position]
        coinDataItem.isEnabled = isChecked
        updateCoinUseCase.invoke(UpdateCoinUseCase.Params(coinDataItem))
    }


}