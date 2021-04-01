package com.app.belcobtm.data.rest.transaction

import com.app.belcobtm.data.rest.transaction.request.*
import com.app.belcobtm.data.rest.transaction.response.*
import com.app.belcobtm.data.rest.transaction.response.hash.*
import kotlinx.coroutines.Deferred
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface TransactionApi {

    @GET("user/{userId}/coin/{coinCode}/transaction-history")
    fun getTransactionsAsync(
        @Path("userId") userId: Int,
        @Path("coinCode") coinCode: String,
        @Query("index") lastListIndex: Int
    ): Deferred<Response<GetTransactionsResponse>>

    @POST("user/{userId}/coin/{coinCode}/submit")
    fun withdrawAsync(
        @Path("userId") userId: Int,
        @Path("coinCode") coinCode: String,
        @Body body: WithdrawRequest
    ): Deferred<Response<ResponseBody>>

    @GET("coin/{coinCode}/transfer-address")
    fun getGiftAddressAsync(
        @Path("coinCode") coinCode: String,
        @Query("phone") phone: String?
    ): Deferred<Response<GetGiftAddressResponse>>

    @POST("user/{userId}/coin/{coinCode}/submit")
    fun sendGiftAsync(
        @Path("userId") userId: Int,
        @Path("coinCode") coinCode: String,
        @Body body: SendGiftRequest
    ): Deferred<Response<ResponseBody>>

    @GET("user/{userId}/limits")
    fun sellGetLimitsAsync(
        @Path("userId") userId: Int
    ): Deferred<Response<LimitsResponse>>

    @POST("user/{userId}/coin/{coinCode}/pre-submit")
    fun sellPreSubmitAsync(
        @Path("userId") userId: Int,
        @Path("coinCode") coinCode: String,
        @Body body: SellPreSubmitRequest
    ): Deferred<Response<SellPreSubmitResponse>>

    @POST("user/{userId}/coin/{coinCode}/submit")
    fun sellAsync(
        @Path("userId") userId: Int,
        @Path("coinCode") coinCode: String,
        @Body body: SellRequest
    ): Deferred<Response<ResponseBody>>

    @POST("user/{userId}/coin/{coinCode}/submit")
    fun exchangeAsync(
        @Path("userId") userId: Int,
        @Path("coinCode") coinFrom: String,
        @Body request: CoinToCoinExchangeRequest
    ): Deferred<Response<ResponseBody>>

    @GET("coin/{coinCode}/utxo")
    fun getUtxoListAsync(
        @Path("coinCode") coinCode: String,
        @Query("xpub") extendedPublicKey: String
    ): Deferred<Response<UtxoListResponse>>

    @GET("coin/{coinCode}/nonce")
    fun getEthereumNonceAsync(
        @Path("coinCode") coinCode: String,
        @Query("address") toAddress: String
    ): Deferred<Response<EthereumResponse>>

    @GET("coin/XRP/current-account")
    fun getRippleBlockHeaderAsync(
        @Query("address") address: String
    ): Deferred<Response<RippleBlockResponse>>

    @GET("coin/XRP/current-account-activated")
    fun checkRippleAccountActivationAsync(
        @Query("address") address: String
    ): Deferred<Response<XRPAccountActivatedResponse>>

    @GET("coin/BNB/current-account")
    fun getBinanceBlockHeaderAsync(
        @Query("address") address: String
    ): Deferred<Response<BinanceBlockResponse>>

    @GET("coin/{coinCode}/current-block")
    fun getTronBlockHeaderAsync(
        @Path("coinCode") coinCode: String
    ): Deferred<Response<TronBlockResponse>>

    @POST("user/{userId}/coin/{coinCode}/submit")
    fun submitRecallAsync(
        @Path("userId") userId: Int,
        @Path("coinCode") coinCode: String,
        @Body body: TradeRecallRequest
    ): Deferred<Response<TronBlockResponse>>

    @POST("user/{userId}/coin/{coinCode}/submit")
    fun submitReserveAsync(
        @Path("userId") userId: Int,
        @Path("coinCode") coinCode: String,
        @Body body: TradeReserveRequest
    ): Deferred<Response<TronBlockResponse>>

    @GET("user/{userId}/coin/{coinCode}/stake-details")
    fun stakeDetailsAsync(
        @Path("userId") userId: Int,
        @Path("coinCode") coinCode: String
    ): Deferred<Response<StakeDetailsResponse>>

    @POST("user/{userId}/coin/{coinCode}/submit")
    fun stakeOrUnStakeAsync(
        @Path("userId") userId: Int,
        @Path("coinCode") coinCode: String,
        @Body body: StakeRequest
    ): Deferred<Response<ResponseBody>>

    @GET("user/{userId}/coin/{coinCode}/transaction-details")
    fun getTransactionDetailsAsync(
        @Path("userId") userId: Int,
        @Path("coinCode") coinCode: String,
        @Query("txId") txId: String
    ): Deferred<Response<TransactionDetailsResponse>>
}