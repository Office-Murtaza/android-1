package com.app.belcobtm.ui.auth.recover_seed

import android.preference.PreferenceManager
import com.app.belcobtm.App
import com.app.belcobtm.api.data_manager.AuthDataManager
import com.app.belcobtm.data.disk.database.CoinDao
import com.app.belcobtm.data.disk.database.CoinEntity
import com.app.belcobtm.data.disk.database.mapToDataItem
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.domain.wallet.item.AccountDataItem
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
            val allCoins = createWalletDB(seed)

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

    private suspend fun createWalletDB(seed: String): List<AccountDataItem> {
        val wallet = HDWallet(seed, "")
        val entityList = LocalCoinType.values().map { createCoinEntity(it, wallet) }
        prefsHelper.apiSeed = seed
        daoCoin.toString()
        daoCoin.insertItemList(entityList)
        return entityList.map { it.mapToDataItem() }
    }

    private fun createCoinEntity(coinType: LocalCoinType, wallet: HDWallet): CoinEntity {
        val privateKey: PrivateKey = wallet.getKeyForCoin(coinType.trustWalletType)
        val address: String = when (coinType) {
            LocalCoinType.BTC -> {
                val extBitcoinPublicKey =
                    wallet.getExtendedPublicKey(Purpose.BIP44, coinType.trustWalletType, HDVersion.XPUB)
                val bitcoinPublicKey = HDWallet.getPublicKeyFromExtended(extBitcoinPublicKey, "m/44'/0'/0'/0/0")
                BitcoinAddress(bitcoinPublicKey, coinType.trustWalletType.p2pkhPrefix()).description()
            }
            else -> coinType.trustWalletType.deriveAddress(privateKey)
        }
        return CoinEntity(coinType, address, Numeric.toHexStringNoPrefix(privateKey.data()))
    }
}