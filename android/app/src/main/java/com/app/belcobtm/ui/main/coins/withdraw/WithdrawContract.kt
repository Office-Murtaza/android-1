package com.app.belcobtm.ui.main.coins.withdraw

import android.content.Context
import com.app.belcobtm.mvp.BaseMvpPresenter
import com.app.belcobtm.mvp.BaseMvpView


object WithdrawContract {
    interface Presenter : BaseMvpPresenter<View> {
        fun getCoinTransactionHash(
            coinId: String,
            toAddress: String,
            coinAmount: Double
        )

        fun validateAddress(coinId: String, walletAddress: String): Boolean
        fun verifySmsCode(code: String)
        fun getTransactionFee(coinName: String): Double
    }

    interface View : BaseMvpView {
        fun onTransactionDone()
        fun openSmsCodeDialog(error: String? = null)
    }
}