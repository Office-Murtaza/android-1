package com.app.belcobtm.api

import com.app.belcobtm.api.model.ServerResponse
import com.app.belcobtm.api.model.param.*
import com.app.belcobtm.api.model.response.*
import io.reactivex.Observable
import retrofit2.http.*


interface ApiInterface {

    @POST("register")
    fun register(@Body registerParam: AuthParam): Observable<ServerResponse<AuthResponse>>

    @POST("recover")
    fun recover(@Body registerParam: AuthParam): Observable<ServerResponse<AuthResponse>>

    @POST("refresh")
    fun refresh(@Body refreshParam: RefreshParam): Observable<ServerResponse<AuthResponse>>

    @POST("user/login")
    fun login(@Body loginParam: AuthParam): Observable<ServerResponse<AuthResponse>>

    @GET("user/{userId}/code/send")
    fun requestSmsCode(@Path("userId") userId: String): Observable<ServerResponse<RequestSmsResponse>>

    @POST("user/{userId}/verify")
    fun verifySmsCode(@Path("userId") userId: String, @Body verifySmsParam: VerifySmsParam): Observable<ServerResponse<VerifySmsResponse>>

    @POST("user/{userId}/coins/add")
    fun addCoins(@Path("userId") userId: String, @Body addCoinsParam: AddCoinsParam): Observable<ServerResponse<AddCoinsResponse>>

    @POST("user/{userId}/coins/compare")
    fun verifyCoins(@Path("userId") userId: String, @Body verifyCoinsParam: AddCoinsParam): Observable<ServerResponse<AddCoinsResponse>>

    @GET("user/{userId}/coins/balance")
    fun getCoins(@Path("userId") userId: String, @Query("coins") coins: ArrayList<String>): Observable<ServerResponse<GetCoinsResponse>>

    @GET("static/atm/address")
    fun getAtmAddress(): Observable<ServerResponse<AtmResponse>>

    @POST("user/{userId}/check/password")
    fun checkPass(
        @Path("userId") userId: String,
        @Body checkPassParam: CheckPassParam
    ): Observable<ServerResponse<CheckPassResponse>>

    @GET("user/{userId}/phone")
    fun getPhone(@Path("userId") userId: String): Observable<ServerResponse<GetPhoneResponse>>

    @POST("user/{userId}/phone")
    fun updatePhone(
        @Path("userId") userId: String,
        @Body updatePhoneParam: UpdatePhoneParam
    ): Observable<ServerResponse<UpdatePhoneResponse>>

    @POST("user/{userId}/phone/confirm")
    fun confirmPhoneSms(
        @Path("userId") userId: String,
        @Body updatePhoneParam: ConfirmPhoneSmsParam
    ): Observable<ServerResponse<ConfirmPhoneSmsResponse>>

    @POST("user/{userId}/unlink")
    fun unlink(@Path("userId") userId: String): Observable<ServerResponse<UpdateResponse>>

    @POST("user/{userId}/password")
    fun changePass(
        @Path("userId") userId: String,
        @Body changePassParam: ChangePassParam
    ): Observable<ServerResponse<UpdateResponse>>

    @GET("user/{userId}/coins/{coinId}/transactions")
    fun getTransactions(
        @Path("userId") userId: String,
        @Path("coinId") coinId: String,
        @Query("index") elementIndex: Int
    ): Observable<ServerResponse<GetTransactionsResponse>>

    @GET("user/{userId}/coins/{coinId}/transactions/utxo/{hex}")
    fun getUtxos(@Path("userId") userId: String, @Path("coinId") coinId: String, @Path("hex") extendedPublicKey: String): Observable<ServerResponse<UtxosResponse>>


    @GET("user/{userId}/coins/{coinId}/sendtx/{hex}")
    fun sendHash(
        @Path("userId") userId: String,
        @Path("coinId") coinId: String,
        @Path("hex") transactionHexToken: String
    ): Observable<ServerResponse<Any>>



    @POST("user/{userId}/coins/{coinId}/transactions/presubmit")
    fun preSubmitTx(
        @Path("userId") userId: String,
        @Path("coinId") coinId: String,
        @Body body: PreTransactionParam
    ): Observable<ServerResponse<PreSubmitResponse>>

 @POST("user/{userId}/coins/{coinId}/transactions/submit")
    fun submitTx(
        @Path("userId") userId: String,
        @Path("coinId") coinId: String,
        @Body body: SendTransactionParam
    ): Observable<ServerResponse<UpdateResponse>>


    @GET("user/{userId}/coins/{coinId}/giftaddress")
    fun giftAddress(
        @Path("userId") userId: String,
        @Path("coinId") coinId: String,
        @Query("phone") phone: String?
    ): Observable<ServerResponse<GiftAddressResponse>>


    @GET("user/{userId}/coins/{coinId}/transactions/currentblock")
    fun getTronBlockHeader(
        @Path("userId") userId: String?,
        @Path("coinId") coinId: String
    ): Observable<ServerResponse<TronBlockResponse>>


    @GET("user/{userId}/coins/BNB/transactions/currentaccount/{address}")
    fun getBNBBlockHeader(
        @Path("userId") userId: String,
        @Path("address") address: String
    ): Observable<ServerResponse<BNBBlockResponse>>

  @GET("user/{userId}/coins/XRP/transactions/currentaccount/{address}")
    fun getXRPBlockHeader(
        @Path("userId") userId: String,
        @Path("address") address: String
    ): Observable<ServerResponse<BNBBlockResponse>>


 @GET("user/{userId}/coins/ETH/transactions/nonce/{address}")
    fun getETHNonce(
        @Path("userId") userId: String,
        @Path("address") address: String
    ): Observable<ServerResponse<ETHResponse>>

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


//    1. Получения UTXO
//    GET /api/v1/user/{userId}/coins/{coinId}/transactions/utxo/{xpub} (BTC, ETH, BCH, LTC)
//
//    2. Получения нонса
//    GET /api/v1/user/{userId}/coins/{coinId}/transactions/nonce/{address} (ETH)
//
//    3. Получения инфо по акаунту
//    GET /api/v1/user/{userId}/coins/{coinId}/transactions/currentaccount/{address} (BNB)
//
//    4. Получения текущего блока
//    GET /api/v1/user/{userId}/coins/{coinId}/transactions/currentblock (TRX)
}