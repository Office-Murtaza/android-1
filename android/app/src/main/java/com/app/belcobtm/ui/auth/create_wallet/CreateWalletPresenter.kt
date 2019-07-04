package com.app.belcobtm.ui.auth.create_wallet

import android.util.Log
import com.app.belcobtm.App
import com.app.belcobtm.api.data_manager.AuthDataManager
import com.app.belcobtm.api.model.ServerException
import com.app.belcobtm.api.model.response.RegisterResponse
import com.app.belcobtm.db.CryptoCoin
import com.app.belcobtm.db.CryptoCoinModel
import com.app.belcobtm.mvp.BaseMvpPresenterImpl
import com.app.belcobtm.util.Optional
import com.app.belcobtm.util.pref
import io.reactivex.Observable
import io.realm.Realm
import org.web3j.utils.Numeric
import wallet.core.jni.BitcoinAddress
import wallet.core.jni.CoinType
import wallet.core.jni.HDWallet
import wallet.core.jni.P2PKHPrefix
import wallet.core.jni.Purpose
import wallet.core.jni.PrivateKey



class CreateWalletPresenter : BaseMvpPresenterImpl<CreateWalletContract.View, AuthDataManager>(),
    CreateWalletContract.Presenter {

//    init {
//        createWallet()
//    }

    override var mDataManager = AuthDataManager()
    private var userId: String = ""
    private var seed: String = ""

    override fun attemptCreateWallet(phone: String, pass: String, confirmPass: String) {
        if (phone.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
            mView?.showError(com.app.belcobtm.R.string.error_all_fields_required)
        } else if (pass.length < 4) {
            mView?.showError(com.app.belcobtm.R.string.error_short_pass)
        } else if (pass != confirmPass) {
            mView?.showError(com.app.belcobtm.R.string.error_confirm_pass)
        } else {
            mView?.showProgress(true)
            mDataManager.registerWallet(phone, pass)
                .flatMap { response ->
                    App.appContext().pref.setSessionApiToken(response.value?.accessToken)
                    App.appContext().pref.setRefreshApiToken(response.value?.refreshToken)
                    App.appContext().pref.setUserId(response.value?.userId)
                    mDataManager.reinitRetrofitClient()

                    return@flatMap Observable.just(response)
                }
                .subscribe({ response: Optional<RegisterResponse> ->
                    mView?.openSmsCodeDialog()
                    mView?.showProgress(false)
                    userId = response.value?.userId.toString()
                }
                    , { error: Throwable ->
                        mView?.showProgress(false)
                        if (error is ServerException) {
                            mView?.showError(error.errorMessage)
                        } else {
                            mView?.showError(error.message)
                        }
                    })
        }
    }

    override fun verifyCode(code: String) {
        mView?.showProgress(true)
        mDataManager.verifySmsCode(code, userId)
            .flatMap { t ->
                val allCoins = createWallet()
                return@flatMap Observable.just(allCoins)
            }
            .flatMap { dbCoins ->
                return@flatMap mDataManager.addCoins(dbCoins, userId)
            }
            .subscribe({ response ->
                mView?.showProgress(false)
                mView?.onWalletCreated(seed)
            }
                , { error: Throwable ->
                    mView?.showProgress(false)
                    if (error is ServerException) {
                        mView?.openSmsCodeDialog(error.errorMessage)
                    } else {
                        mView?.showError(error.message)
                    }

                })
    }


    private fun createWallet(): ArrayList<CryptoCoin> {
        val bitcoin = CoinType.BITCOIN
        val bitcoinCash = CoinType.BITCOINCASH
        val etherum = CoinType.ETHEREUM
        val litecoin = CoinType.LITECOIN
        val binance = CoinType.BINANCE
        val xrp = CoinType.XRP
        val tron = CoinType.TRON

        val wallet = HDWallet(160, "")

        val bitcoinPrivateKey = wallet.getKeyForCoin(bitcoin)
        val bitcoinPrivateKeyStr = Numeric.toHexStringNoPrefix(bitcoinPrivateKey.data())
        val bitcoinPublicKey = bitcoinPrivateKey.getPublicKeySecp256k1(true)
        val bitcoinAddress = BitcoinAddress(bitcoinPublicKey, P2PKHPrefix.BITCOIN.value()).description()

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

        seed = wallet.mnemonic()
        App.appContext().pref.setSeed(seed)

        val realm = Realm.getDefaultInstance()
        val coinModel = CryptoCoinModel()

        coinModel.addCryptoCoin(realm, CryptoCoin("BTC", bitcoinAddress, bitcoinPrivateKeyStr))
        coinModel.addCryptoCoin(realm, CryptoCoin("BCH", bitcoinChAddress, bitcoinChPrivateKeyStr))
        coinModel.addCryptoCoin(realm, CryptoCoin("ETH", etherumAddress, etherumPrivateKeyStr))
        coinModel.addCryptoCoin(realm, CryptoCoin("LTC", litecoinAddress, litecoinPrivateKeyStr))
        coinModel.addCryptoCoin(realm, CryptoCoin("BNB", binanceAddress, binancePrivateKeyStr))
        coinModel.addCryptoCoin(realm, CryptoCoin("TRX", tronAddress, tronPrivateKeyStr))
        coinModel.addCryptoCoin(realm, CryptoCoin("XRP", xrpAddress, xrpPrivateKeyStr))


        return coinModel.getAllCryptoCoin(realm)
    }
}