package com.app.belcobtm.ui.main.coins.details

import com.app.belcobtm.api.model.response.TransactionDetailsResponse
import com.app.belcobtm.mvp.BaseMvpPresenter
import com.app.belcobtm.mvp.BaseMvpView

object DetailsContract {
    interface Presenter : BaseMvpPresenter<View> {
        fun getDetails(coinCode: String, transactionId: String, transactionDbId: String)
    }

    interface View : BaseMvpView {
        fun showTransactionDetails(detailsResponse: TransactionDetailsResponse?)
    }
}