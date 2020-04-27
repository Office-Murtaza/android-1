package com.app.belcobtm.ui.auth.recover_seed

import android.preference.PreferenceManager
import com.app.belcobtm.App
import com.app.belcobtm.api.data_manager.AuthDataManager
import com.app.belcobtm.data.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.db.DbCryptoCoin
import com.app.belcobtm.db.DbCryptoCoinModel
import com.app.belcobtm.mvp.BaseMvpDIPresenterImpl
import io.realm.Realm
import org.web3j.utils.Numeric
import wallet.core.jni.*


class RecoverSeedPresenter : BaseMvpDIPresenterImpl<RecoverSeedContract.View, AuthDataManager>(),
    RecoverSeedContract.Presenter {

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

    private fun createWallet(seed: String): ArrayList<DbCryptoCoin> {
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

        prefsHelper.apiSeed = seed

        val realm = Realm.getDefaultInstance()
        val coinModel = DbCryptoCoinModel()

        coinModel.addCoin(realm, DbCryptoCoin("BTC", bitcoin.value(), bitcoinAddress, bitcoinPrivateKeyStr))
        coinModel.addCoin(realm, DbCryptoCoin("BCH", bitcoinCash.value(), bitcoinChAddress, bitcoinChPrivateKeyStr))
        coinModel.addCoin(realm, DbCryptoCoin("ETH", etherum.value(), etherumAddress, etherumPrivateKeyStr))
        coinModel.addCoin(realm, DbCryptoCoin("LTC", litecoin.value(), litecoinAddress, litecoinPrivateKeyStr))
        coinModel.addCoin(realm, DbCryptoCoin("BNB", binance.value(), binanceAddress, binancePrivateKeyStr))
        coinModel.addCoin(realm, DbCryptoCoin("TRX", tron.value(), tronAddress, tronPrivateKeyStr))
        coinModel.addCoin(realm, DbCryptoCoin("XRP", xrp.value(), xrpAddress, xrpPrivateKeyStr))

        return coinModel.getAllCryptoCoin(realm)
    }
}