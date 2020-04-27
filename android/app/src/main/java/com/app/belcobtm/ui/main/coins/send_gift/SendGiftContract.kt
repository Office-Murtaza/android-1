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

        fun createTransaction(
            context: Context,
            coinModel: CoinModel,
            phone: String,
            coinAmount: Double,
            message: String
        )

        fun completeTransaction(smsCode: String)
        fun getTransactionFee(coinName: String): Double
    }

    interface View : BaseMvpView {
        fun onTransactionDone()
        fun openSmsCodeDialog(error: String? = null)
    }
}