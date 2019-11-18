package com.app.belcobtm.ui.main.coins.send_gift

import android.content.Context
import com.app.belcobtm.api.model.response.CoinModel
import com.app.belcobtm.mvp.BaseMvpPresenter
import com.app.belcobtm.mvp.BaseMvpView
import com.giphy.sdk.core.models.Media


object SendGiftContract {
    interface Presenter : BaseMvpPresenter<View> {
        var phone: String?

        var gifMedia: Media?

        fun getCoinTransactionHash(
            context: Context,
            coinId: CoinModel,
            toAddress: String,
            coinAmount: Double,
            message: String?
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