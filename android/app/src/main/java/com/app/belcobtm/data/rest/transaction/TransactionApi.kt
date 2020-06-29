package com.app.belcobtm.data.rest.transaction

import com.app.belcobtm.data.rest.transaction.request.*
import com.app.belcobtm.data.rest.transaction.response.*
import com.app.belcobtm.data.rest.transaction.response.hash.*
import kotlinx.coroutines.Deferred
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface TransactionApi {

    @GET("user/{userId}/coins/{coinId}/transactions")
    fun getTransactionsAsync(
        @Path("userId") userId: Int,
        @Path("coinId") coinCode: String,
        @Query("index") lastListIndex: Int
    ): Deferred<Response<GetTransactionsResponse>>

    @POST("user/{userId}/coins/{coinId}/transactions/submit")
    fun withdrawAsync(
        @Path("userId") userId: Int,
        @Path("coinId") coinId: String,
        @Body body: WithdrawRequest
    ): Deferred<Response<ResponseBody>>

    @GET("coins/{coinId}/gift-address")
    fun getGiftAddressAsync(
        @Path("userId") userId: Int,
        @Path("coinId") coinId: String,
        @Query("phone") phone: String?
    ): Deferred<Response<GetGiftAddressResponse>>

    @POST("user/{userId}/coins/{coinId}/transactions/submit")
    fun sendGiftAsync(
        @Path("userId") userId: Int,
        @Path("coinId") coinId: String,
        @Body body: SendGiftRequest
    ): Deferred<Response<ResponseBody>>

    @GET("user/{userId}/limits")
    fun sellGetLimitsAsync(
        @Path("userId") userId: Int
    ): Deferred<Response<LimitsResponse>>

    @POST("user/{userId}/coins/{coinId}/transactions/pre-submit")
    fun sellPreSubmitAsync(
        @Path("userId") userId: Int,
        @Path("coinId") coinId: String,
        @Body body: SellPreSubmitRequest
    ): Deferred<Response<SellPreSubmitResponse>>

    @POST("user/{userId}/coins/{coinId}/transactions/submit")
    fun sellAsync(
        @Path("userId") userId: Int,
        @Path("coinId") coinId: String,
        @Body body: SellRequest
    ): Deferred<Response<ResponseBody>>

    @POST("user/{userId}/coins/{coinCode}/transactions/submit")
    fun coinToCoinExchangeAsync(
        @Path("userId") userId: Int,
        @Path("coinCode") coinFrom: String,
        @Body request: CoinToCoinExchangeRequest
    ): Deferred<Response<ResponseBody>>

    @GET("user/{userId}/coins/{coinCode}/trades?tab=1&index=1&sort=1")
    fun tradeGetInfoAsync(
        @Path("userId") userId: Int,
        @Path("coinCode") coinFrom: String
    ): Deferred<Response<TradeInfoResponse>>

    @GET("user/{userId}/coins/{coinCode}/trades?tab=1")
    fun getBuyTradeListAsync(
        @Path("userId") userId: Int,
        @Path("coinCode") coinFrom: String,
        @Query("sort") sort: Int,
        @Query("index") paginationStep: Int
    ): Deferred<Response<TradeInfoResponse>>

    @GET("user/{userId}/coins/{coinCode}/trades?tab=2")
    fun getSellTradeListAsync(
        @Path("userId") userId: Int,
        @Path("coinCode") coinFrom: String,
        @Query("sort") sort: Int,
        @Query("index") paginationStep: Int
    ): Deferred<Response<TradeInfoResponse>>

    @GET("user/{userId}/coins/{coinCode}/trades?tab=3")
    fun getMyTradeListAsync(
        @Path("userId") userId: Int,
        @Path("coinCode") coinFrom: String,
        @Query("sort") sort: Int,
        @Query("index") paginationStep: Int
    ): Deferred<Response<TradeInfoResponse>>

    @GET("user/{userId}/coins/{coinCode}/trades?tab=4")
    fun getOpenTradeListAsync(
        @Path("userId") userId: Int,
        @Path("coinCode") coinFrom: String,
        @Query("sort") sort: Int,
        @Query("index") paginationStep: Int
    ): Deferred<Response<TradeInfoResponse>>

    @POST("user/{userId}/coins/{coinCode}/trade-request")
    fun tradeBuySellAsync(
        @Path("userId") userId: Int,
        @Path("coinCode") toCoin: String,
        @Body request: TradeBuyRequest
    ): Deferred<Response<ResponseBody>>

    @POST("user/{userId}/coins/{coinCode}/trade")
    fun tradeCreateAsync(
        @Path("userId") userId: Int,
        @Path("coinCode") toCoin: String,
        @Body request: TradeCreateRequest
    ): Deferred<Response<ResponseBody>>

    @POST("user/{userId}/location")
    fun tradeSendUserLocationAsync(
        @Path("userId") userId: Int,
        @Body request: TradeLocationRequest
    ): Deferred<Response<ResponseBody>>

    @GET("coins/{coinCode}/utxo")
    fun getUtxoListAsync(
        @Path("userId") userId: Int,
        @Path("coinId") coinId: String,
        @Query("xpub") extendedPublicKey: String
    ): Deferred<Response<UtxoListResponse>>

    @GET("coins/{coinCode}/nonce")
    fun getEthereumNonceAsync(
        @Path("coinCode") coinId: String,
        @Query("address") toAddress: String
    ): Deferred<Response<EthereumResponse>>

    @GET("coins/XRP/current-account")
    fun getRippleBlockHeaderAsync(
        @Query("address") address: String
    ): Deferred<Response<RippleBlockResponse>>

    @GET("coins/BNB/current-account")
    fun getBinanceBlockHeaderAsync(
        @Query("address") address: String
    ): Deferred<Response<BinanceBlockResponse>>

    @GET("coins/{coinCode}/current-block")
    fun getTronBlockHeaderAsync(
        @Path("coinCode") coinCode: String
    ): Deferred<Response<TronBlockResponse>>

    @POST("user/{userId}/coins/{coinCode}/transactions/submit")
    fun submitRecallAsync(
        @Path("userId") userId: Int,
        @Path("coinCode") coinCode: String,
        @Body body: TradeRecallRequest
    ): Deferred<Response<TronBlockResponse>>

    @POST("user/{userId}/coins/{coinCode}/transactions/submit")
    fun submitReserveAsync(
        @Path("userId") userId: Int,
        @Path("coinCode") coinCode: String,
        @Body body: TradeReserveRequest
    ): Deferred<Response<TronBlockResponse>>
}