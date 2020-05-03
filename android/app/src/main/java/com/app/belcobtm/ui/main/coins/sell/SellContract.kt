package com.app.belcobtm.ui.main.coins.sell

import com.app.belcobtm.api.model.response.CoinModel
import com.app.belcobtm.api.model.response.LimitsResponse
import com.app.belcobtm.domain.wallet.item.SellLimitsDataItem
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

        fun verifySmsCode(smsCode: String)

        fun getTransactionFee(coinName: String): Double
        fun bindData(mCoin: CoinModel?)
        fun getDetails()

    }

    interface View : BaseMvpView {
        fun showDoneScreen()
        fun showDoneScreenAnotherAddress(
            addressDestination: String?,
            cryptoAmount: Double
        )

        fun openSmsCodeDialog(error: String? = null)
        fun showLimits(limitsItem: SellLimitsDataItem)
        fun showNewBalanceError()
        fun showPretransactionError()
        fun showErrorAndHideDialogs(resError: Int)
    }
}