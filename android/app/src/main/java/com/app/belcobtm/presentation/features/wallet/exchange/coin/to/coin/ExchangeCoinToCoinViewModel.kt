package com.app.belcobtm.presentation.features.wallet.exchange.coin.to.coin

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.R
import com.app.belcobtm.db.DbCryptoCoinModel
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.CoinFeeDataItem
import com.app.belcobtm.domain.wallet.interactor.CoinToCoinExchangeUseCase
import com.app.belcobtm.domain.wallet.interactor.CreateTransactionUseCase
import com.app.belcobtm.presentation.core.extensions.code
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.features.wallet.IntentCoinItem
import io.realm.Realm
import wallet.core.jni.CoinType

class ExchangeCoinToCoinViewModel(
    val coinFeeItem: CoinFeeDataItem,
    val fromCoinItem: IntentCoinItem,
    val coinItemList: List<IntentCoinItem>,
    private val exchangeUseCase: CoinToCoinExchangeUseCase,
    private val createTransactionUseCase: CreateTransactionUseCase
) : ViewModel() {
    private var transactionHash: String = ""
    val exchangeLiveData: MutableLiveData<LoadingData<String>> = MutableLiveData()
    var toCoinItem: IntentCoinItem? = coinItemList.find { it.coinCode == CoinType.BITCOIN.code() }

    private val realm: Realm = Realm.getDefaultInstance()
    private val dbCryptoCoinModel: DbCryptoCoinModel = DbCryptoCoinModel()

    fun createTransaction(fromCoinAmount: Double) {
        val fromCoinDb = dbCryptoCoinModel.getCryptoCoin(realm, fromCoinItem.coinCode)
        if (fromCoinDb != null) {
            exchangeLiveData.value = LoadingData.Loading()
            createTransactionUseCase.invoke(
                CreateTransactionUseCase.Params(fromCoinDb, fromCoinItem.coinCode, fromCoinAmount)
            ) { either ->
                either.either(
                    { exchangeLiveData.value = LoadingData.Error(it) },
                    { hash ->
                        transactionHash = hash
                        exchangeLiveData.value = LoadingData.Success(TRANSACTION_CREATED)
                    }
                )
            }
        } else {
            exchangeLiveData.value = LoadingData.Error(Failure.MessageError("Please try again"))
        }
    }

    fun exchangeTransaction(
        code: String,
        amountFromCoin: Double
    ) {
        exchangeLiveData.value = LoadingData.Loading()
        exchangeUseCase.invoke(
            CoinToCoinExchangeUseCase.Params(
                code,
                amountFromCoin,
                fromCoinItem.coinCode,
                toCoinItem?.coinCode ?: "",
                transactionHash
            )
        ) { either ->
            either.either(
                { exchangeLiveData.value = LoadingData.Error(it) },
                { exchangeLiveData.value = LoadingData.Success(TRANSACTION_EXCHANGED) }
            )
        }
    }

    fun getCoinTypeList(): List<CoinType> = listOf(
        CoinType.BITCOIN,
        CoinType.ETHEREUM,
        CoinType.BITCOINCASH,
        CoinType.LITECOIN,
        CoinType.BINANCE,
        CoinType.TRON,
        CoinType.XRP
    )

    companion object {
        const val TRANSACTION_CREATED = "transaction_created"
        const val TRANSACTION_EXCHANGED = "transaction_exchanged"
    }
}