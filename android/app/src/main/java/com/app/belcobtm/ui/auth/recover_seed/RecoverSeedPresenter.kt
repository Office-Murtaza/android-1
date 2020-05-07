package com.app.belcobtm.ui.auth.recover_seed

import android.preference.PreferenceManager
import com.app.belcobtm.App
import com.app.belcobtm.api.data_manager.AuthDataManager
import com.app.belcobtm.data.disk.database.CoinDao
import com.app.belcobtm.data.disk.database.CoinEntity
import com.app.belcobtm.data.disk.database.mapToDataItem
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.mvp.BaseMvpDIPresenterImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.web3j.utils.Numeric
import wallet.core.jni.*


class RecoverSeedPresenter : BaseMvpDIPresenterImpl<RecoverSeedContract.View, AuthDataManager>(),
    RecoverSeedContract.Presenter, KoinComponent {
    private val daoCoin: CoinDao by inject()

    //TODO need migrate to dependency koin after refactoring
    private val prefsHelper: SharedPreferencesHelper by lazy {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.appContext())
        SharedPreferencesHelper(sharedPreferences)
    }

    override fun injectDependency() {
        presenterComponent.inject(this)
    }

    override fun verifySeed(seed: String) {
        mView?.showProgress(true)
        CoroutineScope(Dispatchers.IO).launch {
            val allCoins = createWallet(seed)

            val userId = prefsHelper.userId.toString()
            mDataManager.verifyCoins(userId, allCoins)
                .subscribe({ response ->
                    mView?.showProgress(false)
                    mView?.onSeedVerifyed()
                }
                    , { error: Throwable ->
                        mView?.showProgress(false)
                        mView?.showError(error.message)
                    })
        }
    }

    private suspend fun createWallet(seed: String): List<CoinDataItem> {
        val bitcoin = CoinType.BITCOIN
        val bitcoinCash = CoinType.BITCOINCASH
        val etherum = CoinType.ETHEREUM
        val litecoin = CoinType.LITECOIN
        val binance = CoinType.BINANCE
        val xrp = CoinType.XRP
        val tron = CoinType.TRON

        val wallet = HDWallet(seed, "")

        val bitcoinPrivateKey = wallet.getKeyForCoin(bitcoin)
        val bitcoinPrivateKeyStr = Numeric.toHexStringNoPrefix(bitcoinPrivateKey.data())
        val extBitcoinPublicKey =
            wallet.getExtendedPublicKey(Purpose.BIP44, bitcoin, HDVersion.XPUB)

        val bitcoinPublicKey =
            HDWallet.getPublicKeyFromExtended(extBitcoinPublicKey, "m/44'/0'/0'/0/0")
        val bitcoinAddress =
            BitcoinAddress(bitcoinPublicKey, CoinType.BITCOIN.p2pkhPrefix()).description()

        val bitcoinChPrivateKey = wallet.getKeyForCoin(bitcoinCash)
        val bitcoinChPrivateKeyStr = Numeric.toHexStringNoPrefix(bitcoinChPrivateKey.data())
        val bitcoinChAddress = bitcoinCash.deriveAddress(bitcoinChPrivateKey)

        val etherumPrivateKey = wallet.getKeyForCoin(etherum)
        val etherumPrivateKeyStr = Numeric.toHexStringNoPrefix(etherumPrivateKey.data())
        val etherumAddress = etherum.deriveAddress(etherumPrivateKey)

        val litecoinPrivateKey = wallet.getKeyForCoin(litecoin)
        val litecoinPrivateKeyStr = Numeric.toHexStringNoPrefix(litecoinPrivateKey.data())
        val litecoinAddress = litecoin.deriveAddress(litecoinPrivateKey)

        val binancePrivateKey = wallet.getKeyForCoin(binance)
        val binancePrivateKeyStr = Numeric.toHexStringNoPrefix(binancePrivateKey.data())
        val binanceAddress = binance.deriveAddress(binancePrivateKey)

        val xrpPrivateKey = wallet.getKeyForCoin(xrp)
        val xrpPrivateKeyStr = Numeric.toHexStringNoPrefix(xrpPrivateKey.data())
        val xrpAddress = xrp.deriveAddress(xrpPrivateKey)

        val tronPrivateKey = wallet.getKeyForCoin(tron)
        val tronPrivateKeyStr = Numeric.toHexStringNoPrefix(tronPrivateKey.data())
        val tronAddress = tron.deriveAddress(tronPrivateKey)
        val entityList = listOf(
            CoinEntity(LocalCoinType.BTC, bitcoinAddress, bitcoinPrivateKeyStr),
            CoinEntity(LocalCoinType.BCH, bitcoinChAddress, bitcoinChPrivateKeyStr),
            CoinEntity(LocalCoinType.ETH, etherumAddress, etherumPrivateKeyStr),
            CoinEntity(LocalCoinType.LTC, litecoinAddress, litecoinPrivateKeyStr),
            CoinEntity(LocalCoinType.BNB, binanceAddress, binancePrivateKeyStr),
            CoinEntity(LocalCoinType.TRX, tronAddress, tronPrivateKeyStr),
            CoinEntity(LocalCoinType.XRP, xrpAddress, xrpPrivateKeyStr)
        )
        prefsHelper.apiSeed = seed
        daoCoin.toString()
        daoCoin.insertItemList(entityList)
        return entityList.map { it.mapToDataItem() }
    }
}