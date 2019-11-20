package com.app.belcobtm.ui.main.coins.sell

import android.content.Context
import com.app.belcobtm.api.model.response.CoinModel
import com.app.belcobtm.api.model.response.LimitsResponse
import com.app.belcobtm.mvp.BaseMvpPresenter
import com.app.belcobtm.mvp.BaseMvpView


object SellContract {
    interface Presenter : BaseMvpPresenter<View> {
        fun preSubmit(
            fiatAmount: Int,
            cryptoAmount: Double,
            balance: Double,
            checked: Boolean
        )

        //     fun validateAddress(coinId: String, walletAddress: String): Boolean
        fun verifySmsCode(code: String)

        fun getTransactionFee(coinName: String): Double
        fun bindData(mCoin: CoinModel?)
        fun getDetails()

    }

    interface View : BaseMvpView {
        fun onTransactionDone(
            anotherAddress: Boolean,
            addressDestination: String?,
            cryptoResultAmount: Double
        )
        fun openSmsCodeDialog(error: String? = null)
        fun showLimits(value: LimitsResponse?)
        fun showNewBalanceError()
        fun showPretransactionError()
        fun showErrorAndHideDialogs(errorMessage: String?)
    }
}