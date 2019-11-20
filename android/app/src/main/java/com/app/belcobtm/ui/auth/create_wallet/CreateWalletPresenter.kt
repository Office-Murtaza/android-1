package com.app.belcobtm.ui.auth.create_wallet

import com.app.belcobtm.App
import com.app.belcobtm.api.data_manager.AuthDataManager
import com.app.belcobtm.api.model.ServerException
import com.app.belcobtm.api.model.response.AuthResponse
import com.app.belcobtm.db.DbCryptoCoin
import com.app.belcobtm.db.DbCryptoCoinModel
import com.app.belcobtm.mvp.BaseMvpDIPresenterImpl
import com.app.belcobtm.util.Optional
import com.app.belcobtm.util.pref
import io.reactivex.Observable
import io.realm.Realm
import org.spongycastle.crypto.DerivationParameters
import org.web3j.utils.Numeric
import wallet.core.jni.*


class CreateWalletPresenter : BaseMvpDIPresenterImpl<CreateWalletContract.View, AuthDataManager>(),
    CreateWalletContract.Presenter {

//    init {
//        createWallet()
//    }

    private var userId: String = ""
    private var seed: String = ""

    override fun injectDependency() {
        presenterComponent.inject(this)
    }

    override fun attemptCreateWallet(phone: String, pass: String, confirmPass: String) {
        if (phone.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
            mView?.showError(com.app.belcobtm.R.string.error_all_fields_required)
        } else if (pass.length < 6) {
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
                    mDataManager.updateToken()

                    return@flatMap Observable.just(response)
                }
                .subscribe({ response: Optional<AuthResponse> ->
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
        mDataManager.verifySmsCode(userId, code)
            .flatMap { t ->
                val allCoins = createWallet()
                return@flatMap Observable.just(allCoins)
            }
            .flatMap { dbCoins ->
                return@flatMap mDataManager.addCoins(userId, dbCoins)
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


    private fun createWallet(): ArrayList<DbCryptoCoin> {
        val bitcoin = CoinType.BITCOIN
        val bitcoinCash = CoinType.BITCOINCASH
        val etherum = CoinType.ETHEREUM
        val litecoin = CoinType.LITECOIN
        val binance = CoinType.BINANCE
        val xrp = CoinType.XRP
        val tron = CoinType.TRON

        val wallet = HDWallet(128, "")

        val bitcoinPrivateKey = wallet.getKeyForCoin(bitcoin)
        val bitcoinPrivateKeyStr = Numeric.toHexStringNoPrefix(bitcoinPrivateKey.data())
        val extBitcoinPublicKey = wallet.getExtendedPublicKey(Purpose.BIP44, bitcoin, HDVersion.XPUB)
        val bitcoinPublicKey = HDWallet.getPublicKeyFromExtended(extBitcoinPublicKey, "m/44'/145'/0'/0/2")
        val bitcoinAddress = BitcoinAddress(bitcoinPublicKey, CoinType.BITCOIN.p2pkhPrefix()).description()
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
        val coinModel = DbCryptoCoinModel()

        coinModel.addCryptoCoin(realm, DbCryptoCoin("BTC", bitcoin.value(), bitcoinAddress, bitcoinPrivateKeyStr))
        coinModel.addCryptoCoin(
            realm,
            DbCryptoCoin("BCH", bitcoinCash.value(), bitcoinChAddress, bitcoinChPrivateKeyStr)
        )
        coinModel.addCryptoCoin(realm, DbCryptoCoin("ETH", etherum.value(), etherumAddress, etherumPrivateKeyStr))
        coinModel.addCryptoCoin(realm, DbCryptoCoin("LTC", litecoin.value(), litecoinAddress, litecoinPrivateKeyStr))
        coinModel.addCryptoCoin(realm, DbCryptoCoin("BNB", binance.value(), binanceAddress, binancePrivateKeyStr))
        coinModel.addCryptoCoin(realm, DbCryptoCoin("TRX", tron.value(), tronAddress, tronPrivateKeyStr))
        coinModel.addCryptoCoin(realm, DbCryptoCoin("XRP", xrp.value(), xrpAddress, xrpPrivateKeyStr))


        return coinModel.getAllCryptoCoin(realm)
    }
}