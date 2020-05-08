package com.app.belcobtm.api.data_manager

import com.app.belcobtm.api.model.param.VerifySmsParam
import com.app.belcobtm.api.model.response.TransactionDetailsResponse
import com.app.belcobtm.api.model.response.VerifySmsResponse
import com.app.belcobtm.presentation.core.Optional
import io.reactivex.Observable

open class WithdrawDataManager : BaseDataManager() {

    fun verifySmsCode(userId: String, smsCode: String): Observable<Optional<VerifySmsResponse>> {
        return genObservable(api.verifySmsCode(userId, VerifySmsParam(smsCode)))
    }

    fun getTransactionDetails(
        userId: String?,
        coinId: String?,
        txid: String?,
        txDbId: String?
    ): Observable<Optional<TransactionDetailsResponse>> {
        return if (txid != null) {
            genObservable(api.getTransactionDetails(userId, coinId, txid))
        } else {
            genObservable(api.getTransactionDetailsByTxDbId(userId, coinId, txDbId))
        }
    }
}