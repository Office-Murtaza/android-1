package com.app.belcobtm.presentation.features.wallet.add

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.R
import com.app.belcobtm.db.DbCryptoCoinModel
import com.app.belcobtm.presentation.features.wallet.add.adapter.AddWalletCoinItem
import io.realm.Realm

class AddWalletViewModel : ViewModel() {
    val coinListLiveData: MutableLiveData<List<AddWalletCoinItem>> = MutableLiveData()
    private val realm = Realm.getDefaultInstance()
    private val dbCoinModel = DbCryptoCoinModel()
    private val dbCoinList = dbCoinModel.getAllCryptoCoin(realm)

    init {
        coinListLiveData.value = dbCoinList.map {
            AddWalletCoinItem(
                getCoinResIconByType(it.coinType),
                getCoinResNameByType(it.coinType),
                it.visible
            )
        }
    }

    fun changeCoinState(position: Int, isChecked: Boolean) {
        val dbCoin = dbCoinList[position]
        dbCoin.visible = isChecked
        dbCoinModel.editCryptoCoin(realm, dbCoin)
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