package com.app.belcobtm.presentation.features.wallet.add

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.R
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.domain.wallet.interactor.GetCoinListUseCase
import com.app.belcobtm.domain.wallet.interactor.UpdateCoinUseCase
import com.app.belcobtm.presentation.features.wallet.add.adapter.AddWalletCoinItem

class AddWalletViewModel(
    coinListUseCase: GetCoinListUseCase,
    private val updateCoinUseCase: UpdateCoinUseCase
) : ViewModel() {
    val coinListLiveData: MutableLiveData<List<AddWalletCoinItem>> = MutableLiveData()
    private val coinDataList: MutableList<CoinDataItem> = mutableListOf()

    init {
        coinListUseCase.invoke(Unit) { either ->
            either.either(
                { /* empty */ },
                { result ->
                    coinDataList.addAll(result)
                    coinListLiveData.value = coinDataList.map {
                        AddWalletCoinItem(
                            getCoinResIconByType(it.type.name),
                            getCoinResNameByType(it.type.name),
                            it.isEnabled
                        )
                    }
                }
            )
        }
    }

    fun changeCoinState(position: Int, isChecked: Boolean) {
        val coinDataItem = coinDataList[position]
        coinDataItem.isEnabled = isChecked
        updateCoinUseCase.invoke(UpdateCoinUseCase.Params(coinDataItem)) {}
    }

    private fun getCoinResIconByType(coinType: String) = when (coinType) {
        "BTC" -> R.drawable.ic_coin_bitcoin
        "ETH" -> R.drawable.ic_coin_ethereum
        "BCH" -> R.drawable.ic_coin_bitcoin_cash
        "LTC" -> R.drawable.ic_coin_litecoin
        "BNB" -> R.drawable.ic_coin_binance
        "TRX" -> R.drawable.ic_coin_tron
        "XRP" -> R.drawable.ic_coin_ripple
        else -> 0
    }

    private fun getCoinResNameByType(coinName: String): Int = when (coinName) {
        "BTC" -> R.string.coin_name_bitcoin
        "ETH" -> R.string.coin_name_ethereum
        "BCH" -> R.string.coin_name_bitcoin_cash
        "LTC" -> R.string.coin_name_litecoin
        "BNB" -> R.string.coin_name_binance
        "TRX" -> R.string.coin_name_tron
        "XRP" -> R.string.coin_name_ripple
        else -> R.string.unknown
    }
}