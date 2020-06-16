package com.app.belcobtm.api

import com.app.belcobtm.api.model.ServerResponse
import com.app.belcobtm.api.model.param.*
import com.app.belcobtm.api.model.response.*
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


interface ApiInterface {

    @POST("recover")
    fun recover(@Body registerParam: AuthParam): Observable<ServerResponse<AuthResponse>>

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

    @GET("terminal/locations")
    fun getAtmAddress(): Observable<ServerResponse<AtmResponse>>

    @POST("user/{userId}/password/verify")
    fun checkPass(
        @Path("userId") userId: String,
        @Body checkPassParam: CheckPassParam
    ): Observable<ServerResponse<CheckPassResponse>>

    @GET("user/{userId}/phone")
    fun getPhone(@Path("userId") userId: String): Observable<ServerResponse<GetPhoneResponse>>

    @POST("user/{userId}/phone/update")
    fun updatePhone(
        @Path("userId") userId: String,
        @Body updatePhoneParam: UpdatePhoneParam
    ): Observable<ServerResponse<UpdatePhoneResponse>>

    @POST("user/{userId}/phone/verify")
    fun confirmPhoneSms(
        @Path("userId") userId: String,
        @Body updatePhoneParam: ConfirmPhoneSmsParam
    ): Observable<ServerResponse<ConfirmPhoneSmsResponse>>

    @GET("user/{userId}/unlink")
    fun unlink(@Path("userId") userId: String): Observable<ServerResponse<UpdateResponse>>

    @POST("user/{userId}/password/update")
    fun changePass(
        @Path("userId") userId: String,
        @Body changePassParam: ChangePassParam
    ): Observable<ServerResponse<UpdateResponse>>

    @GET("user/{userId}/coins/{coinId}/transactions/limits")
    fun getLimits(
        @Path("userId") userId: String,
        @Path("coinId") coinId: String?
    ): Observable<ServerResponse<LimitsResponse>>

    @GET("user/{userId}/coins/{coinId}/transaction/{txid}")
    fun getTransactionDetails(
        @Path("userId") userId: String?,
        @Path("coinId") coinId: String?,
        @Path("txid") txid: String?
    ): Observable<ServerResponse<TransactionDetailsResponse>>

    @GET("user/{userId}/coins/{coinId}/transaction/{txDbId}")
    fun getTransactionDetailsByTxDbId(
        @Path("userId") userId: String?,
        @Path("coinId") coinId: String?,
        @Path("txDbId") txDbId: String?
    ): Observable<ServerResponse<TransactionDetailsResponse>>
}