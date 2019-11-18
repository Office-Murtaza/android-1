package com.app.belcobtm.ui.main.coins.details

import com.app.belcobtm.api.model.response.CoinModel
import com.app.belcobtm.api.model.response.TransactionDetailsResponse
import com.app.belcobtm.api.model.response.TransactionModel
import com.app.belcobtm.mvp.BaseMvpPresenter
import com.app.belcobtm.mvp.BaseMvpView


object DetailsContract {
    interface Presenter : BaseMvpPresenter<View> {
        fun getDetails()
        fun bindData(mCoin: CoinModel?, transaction: TransactionModel)


    }

    interface View : BaseMvpView {
        fun showTransactionDetails(detailsResponse: TransactionDetailsResponse?)


    }
}