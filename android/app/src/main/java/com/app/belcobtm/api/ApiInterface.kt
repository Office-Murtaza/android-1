package com.app.belcobtm.api

import com.app.belcobtm.api.model.ServerResponse
import com.app.belcobtm.api.model.param.*
import com.app.belcobtm.api.model.response.*
import io.reactivex.Observable
import retrofit2.http.*


interface ApiInterface {

    @POST("user/{userId}/code/verify")
    fun verifySmsCode(
        @Path("userId") userId: String,
        @Body verifySmsParam: VerifySmsParam
    ): Observable<ServerResponse<VerifySmsResponse>>

    @POST("user/{userId}/coins/compare")
    fun verifyCoins(
        @Path("userId") userId: String,
        @Body verifyCoinsParam: AddCoinsParam
    ): Observable<ServerResponse<AddCoinsResponse>>


    @POST("user/{userId}/password/verify")
    fun checkPass(
        @Path("userId") userId: String,
        @Body checkPassParam: CheckPassParam
    ): Observable<ServerResponse<CheckPassResponse>>

    @GET("user/{userId}/coin/{coinCode}/transaction-details")
    fun getTransactionDetails(
        @Path("userId") userId: String?,
        @Path("coinCode") coinCode: String?,
        @Query("txId") txId: String?
    ): Observable<ServerResponse<TransactionDetailsResponse>>

    @GET("user/{userId}/coin/{coinCode}/transaction-details")
    fun getTransactionDetailsByTxDbId(
        @Path("userId") userId: String?,
        @Path("coinCode") coinCode: String?,
        @Query("txId") txDbId: String?
    ): Observable<ServerResponse<TransactionDetailsResponse>>
}