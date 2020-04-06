package com.app.belcobtm.presentation.features.wallet.exchange.coin.to.coin

import android.preference.PreferenceManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.App
import com.app.belcobtm.api.model.ServerException
import com.app.belcobtm.api.model.param.trx.Trx
import com.app.belcobtm.data.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.db.DbCryptoCoinModel
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.CoinFeeDataItem
import com.app.belcobtm.domain.wallet.interactor.CoinToCoinExchangeUseCase
import com.app.belcobtm.presentation.core.extensions.code
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.features.wallet.CryptoHashHelper
import com.app.belcobtm.presentation.features.wallet.IntentCoinItem
import com.google.gson.Gson
import io.reactivex.disposables.CompositeDisposable
import io.realm.Realm
import org.json.JSONObject
import wallet.core.jni.CoinType
import wallet.core.jni.HDWallet
import java.net.HttpURLConnection

class ExchangeCoinToCoinViewModel(
    val coinFeeItem: CoinFeeDataItem,
    val fromCoinItem: IntentCoinItem,
    val coinItemList: List<IntentCoinItem>,
    private val exchangeUseCase: CoinToCoinExchangeUseCase
) : ViewModel() {
    val exchangeLiveData: MutableLiveData<LoadingData<Unit>> = MutableLiveData()
    var toCoinItem: IntentCoinItem? = coinItemList.find { it.coinCode == CoinType.BITCOIN.code() }

    fun getCoinTypeList(): List<CoinType> = listOf(
        CoinType.BITCOIN,
        CoinType.ETHEREUM,
        CoinType.BITCOINCASH,
        CoinType.LITECOIN,
        CoinType.BINANCE,
        CoinType.TRON,
        CoinType.XRP
    )

    private fun exchangeCoinToCoin(hex: String, amountFromCoin: Double) {
        exchangeUseCase.invoke(
            CoinToCoinExchangeUseCase.Params(
                amountFromCoin,
                fromCoinItem.coinCode,
                toCoinItem?.coinCode ?: "",
                hex
            )
        ) { either ->
            exchangeLiveData.value = LoadingData.Loading()
            either.either(
                { exchangeLiveData.value = LoadingData.Error(it) },
                { exchangeLiveData.value = LoadingData.Success(it) }
            )
        }
    }

    //TODO Trash code, now hard to change hash realization, need refactoring sprint
    private val prefsHelper: SharedPreferencesHelper by lazy {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.appContext())
        SharedPreferencesHelper(sharedPreferences)
    }
    private val realm = Realm.getDefaultInstance()
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val hashHelper: CryptoHashHelper = CryptoHashHelper()
    private val coinModel = DbCryptoCoinModel()

    fun exchange(amountFromCoin: Double) {
        exchangeLiveData.value = LoadingData.Loading()

        val hdWallet = HDWallet(prefsHelper.apiSeed, "")
        val coinDbModel = coinModel.getCryptoCoin(realm, fromCoinItem.coinCode)
        val coinType = CoinType.createFromValue(coinDbModel!!.coinTypeId)

        val request = hashHelper.getCoinTransactionHashObs(
            hdWallet,
            coinFeeItem.serverWalletAddress,
            coinType,
            amountFromCoin,
            coinDbModel
        ).subscribe(
            { responseJson ->
                if (!JSONObject(responseJson).isNull(KEY_TX_ID)) {
                    val hash = JSONObject(responseJson).getString(KEY_TX_ID)
                    exchangeCoinToCoin(hash, amountFromCoin)
                } else {
                    exchangeLiveData.value =  LoadingData.Error(Failure.MessageError("Transaction error"))
                }
            },
            { throwable ->
                exchangeLiveData.value = when {
                    throwable is ServerException
                            && throwable.code == HttpURLConnection.HTTP_FORBIDDEN -> LoadingData.Error(Failure.TokenError)
                    throwable is ServerException -> LoadingData.Error(Failure.MessageError(throwable.errorMessage))
                    else -> LoadingData.Error(Failure.MessageError(throwable.message))
                }
            }
        )
        compositeDisposable.add(request)
    }

    companion object {
        private const val KEY_TX_ID = "txID"
    }
}