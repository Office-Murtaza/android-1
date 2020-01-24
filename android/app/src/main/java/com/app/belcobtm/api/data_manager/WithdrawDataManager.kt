package com.app.belcobtm.api.data_manager

import com.app.belcobtm.api.TempUtxoRetrofitClient
import com.app.belcobtm.api.model.param.PreTransactionParam
import com.app.belcobtm.api.model.param.SendTransactionParam
import com.app.belcobtm.api.model.param.VerifySmsParam
import com.app.belcobtm.api.model.response.*
import com.app.belcobtm.core.Optional
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.http.Body

open class WithdrawDataManager : BaseDataManager() {
    private val tempUtxoApi = TempUtxoRetrofitClient.instance.apiInterface

    private fun <T> utxoApplySchedulers(observable: Observable<T>): Observable<T> {
        return observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }

    fun getUtxos(publicKey: String): Observable<ArrayList<UtxoItem>> {
        return utxoApplySchedulers(tempUtxoApi.getUtxo(publicKey))
    }

    fun getBTCUtxos(
        userId: String,
        coinId: String,
        extendedPublicKey: String
    ): Observable<Optional<UtxosResponse>> {
        return genObservable(api.getUtxos(userId, coinId, extendedPublicKey))
    }

    fun sendHash(
        userId: String,
        coinId: String,
        transactionHexToken: String
    ): Observable<Optional<Any>> {
        return genObservable(api.sendHash(userId, coinId, transactionHexToken))
    }


    fun requestSmsCode(userId: String): Observable<Optional<RequestSmsResponse>> {
        return genObservable(api.requestSmsCode(userId))
    }

    fun verifySmsCode(userId: String, smsCode: String): Observable<Optional<VerifySmsResponse>> {
        return genObservable(api.verifySmsCode(userId, VerifySmsParam(smsCode)))
    }


    fun preSubmitTx(
        userId: String,
        coinId: String,
        @Body body: PreTransactionParam
    ): Observable<Optional<PreSubmitResponse>> = genObservable(
        api.preSubmitTx(
            userId,
            coinId,
            body
        )
    )

    fun submitTx(
        userId: String,
        coinId: String,
        body: SendTransactionParam
    ): Observable<Optional<UpdateResponse>> = genObservable(
        api.submitTx(
            userId,
            coinId,
            body
        )
    )

    fun giftAddress(
        userId: String,
        coinId: String,
        phone: String?
    ): Observable<Optional<GiftAddressResponse>> {
        return genObservable(api.giftAddress(userId, coinId, phone))
    }

    fun getTronBlockHeader(
        userId: String?,
        coinId: String
    ): Observable<Optional<TronBlockResponse>> {
        return genObservable(api.getTronBlockHeader(userId, coinId))
    }

    fun getBNBBlockHeader(
        userId: String,
        address: String
    ): Observable<Optional<BNBBlockResponse>> =
        genObservable(api.getBNBBlockHeader(userId))

    fun getXRPBlockHeader(
        userId: String,
        address: String
    ): Observable<Optional<BNBBlockResponse>> =
        genObservable(api.getXRPBlockHeader(userId))

    fun getETHNonce(
        userId: String,
        address: String
    ): Observable<Optional<ETHResponse>> =
        genObservable(api.getETHNonce(userId))

    fun getLimits(
        userId: String,
        coinId: String?
    ): Observable<Optional<LimitsResponse>> =
        genObservable(api.getLimits(userId, coinId))

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