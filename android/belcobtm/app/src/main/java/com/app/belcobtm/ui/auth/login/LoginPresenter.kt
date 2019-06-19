package com.app.belcobtm.ui.auth.login

import com.app.belcobtm.mvp.BaseMvpPresenterImpl
import wallet.core.jni.CoinType
import wallet.core.jni.HDWallet


class LoginPresenter : BaseMvpPresenterImpl<LoginContract.View>(), LoginContract.Presenter {
    //private val mDataManager = LoginDataManager()


    override fun login(email: String?, password: String?) {
        mView?.showProgress(true)
        //todo
    }

    override fun createWallet() {
        val bitcoin = CoinType.BITCOIN
        val mnemonic = "ripple scissors kick mammal hire column oak again sun offer wealth tomorrow wagon turn fatal"
        val passphrase = "TREZOR"
        val wallet = HDWallet(mnemonic, passphrase)
        val privateKey = wallet.getKeyForCoin(bitcoin)
        val address = bitcoin.deriveAddress(privateKey)

        mView?.showMessage("coinType: $bitcoin\naddress: $address\nprivateKey: $privateKey\nmnemonic: $mnemonic\npassphrase: $passphrase")

    }
}