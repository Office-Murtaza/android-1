package com.app.belcobtm.presentation.features.wallet.exchange.coin.to.coin

import android.preference.PreferenceManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.App
import com.app.belcobtm.api.model.ServerException
import com.app.belcobtm.data.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.db.DbCryptoCoinModel
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.tools.SendToDeviceSmsCodeUseCase
import com.app.belcobtm.domain.tools.VerifySmsCodeUseCase
import com.app.belcobtm.domain.wallet.CoinFeeDataItem
import com.app.belcobtm.domain.wallet.interactor.CoinToCoinExchangeUseCase
import com.app.belcobtm.presentation.core.extensions.code
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.features.wallet.CryptoHashHelper
import com.app.belcobtm.presentation.features.wallet.IntentCoinItem
import io.reactivex.disposables.CompositeDisposable
import io.realm.Realm
import wallet.core.jni.CoinType
import wallet.core.jni.HDWallet
import java.net.HttpURLConnection

class ExchangeCoinToCoinViewModel(
    val coinFeeItem: CoinFeeDataItem,
    val fromCoinItem: IntentCoinItem,
    val coinItemList: List<IntentCoinItem>,
    private val exchangeUseCase: CoinToCoinExchangeUseCase,
    private val sendToDeviceSmsCodeUseCase: SendToDeviceSmsCodeUseCase,
    private val verifySmsCodeUseCase: VerifySmsCodeUseCase
) : ViewModel() {
    val exchangeLiveData: MutableLiveData<LoadingData<String>> = MutableLiveData()
    var toCoinItem: IntentCoinItem? = coinItemList.find { it.coinCode == CoinType.BITCOIN.code() }

    //TODO Trash values, need refactoring sprint
    private val prefsHelper: SharedPreferencesHelper by lazy {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.appContext())
        SharedPreferencesHelper(sharedPreferences)
    }
    private val realm = Realm.getDefaultInstance()
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val hashHelper: CryptoHashHelper = CryptoHashHelper()
    private val coinModel = DbCryptoCoinModel()
    private var transactionHash: String = ""

    @Deprecated("Trash code, now hard to change hash realization, need refactoring sprint")
    fun createTransaction(amountFromCoin: Double) {
        exchangeLiveData.value = LoadingData.Loading()
        val coinDbModel = coinModel.getCryptoCoin(realm, fromCoinItem.coinCode)
        val coinType = CoinType.createFromValue(coinDbModel!!.coinTypeId)
        val request = hashHelper.getCoinTransactionHashObs(
            HDWallet(prefsHelper.apiSeed, ""),
            coinFeeItem.serverWalletAddress,
            coinType,
            amountFromCoin,
            coinDbModel
        ).subscribe(
            { hash ->
                transactionHash = hash
                sendSmsToDevice()
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

    fun exchangeTransaction(
        code: String,
        amountFromCoin: Double
    ) = verifySmsCodeUseCase.invoke(VerifySmsCodeUseCase.Params(code)) { either ->
        either.either(
            { exchangeLiveData.value = LoadingData.Error(it) },
            { exchange(amountFromCoin) }
        )
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

    private fun sendSmsToDevice() = sendToDeviceSmsCodeUseCase.invoke(Unit) { either ->
        either.either(
            { exchangeLiveData.value = LoadingData.Error(it) },
            { exchangeLiveData.value = LoadingData.Success(TRANSACTION_CREATED) }
        )
    }

    private fun exchange(amountFromCoin: Double) {
        exchangeUseCase.invoke(
            CoinToCoinExchangeUseCase.Params(
                amountFromCoin,
                fromCoinItem.coinCode,
                toCoinItem?.coinCode ?: "",
                transactionHash
            )
        ) { either ->
            exchangeLiveData.value = LoadingData.Loading()
            either.either(
                { exchangeLiveData.value = LoadingData.Error(it) },
                { exchangeLiveData.value = LoadingData.Success(TRANSACTION_EXCHANGED) }
            )
        }
    }

    companion object {
        const val TRANSACTION_CREATED = "transaction_created"
        const val TRANSACTION_EXCHANGED = "transaction_exchanged"
    }
}