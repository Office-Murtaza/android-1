package com.app.belcobtm.data.rest.wallet

import com.app.belcobtm.data.rest.wallet.request.*
import com.app.belcobtm.data.rest.wallet.response.*
import com.app.belcobtm.data.rest.wallet.response.hash.*
import kotlinx.coroutines.Deferred
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface WalletApi {

    @POST("user/{userId}/coins/{coinId}/transactions/submit")
    fun withdrawAsync(
        @Path("userId") userId: Int,
        @Path("coinId") coinId: String,
        @Body body: WithdrawRequest
    ): Deferred<Response<ResponseBody>>

    @GET("user/{userId}/coins/{coinId}/giftaddress")
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

    @GET("user/{userId}/coins/{coinId}/transactions/limits")
    fun sellGetLimitsAsync(
        @Path("userId") userId: Int,
        @Path("coinId") coinId: String
    ): Deferred<Response<LimitsResponse>>

    @POST("user/{userId}/coins/{coinId}/transactions/presubmit")
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

    @GET("user/{userId}/coins/{coinCode}/trades")
    fun tradeGetInfoAsync(
        @Path("userId") userId: Int,
        @Path("coinCode") coinFrom: String
    ): Deferred<Response<TradeInfoResponse>>

    @POST("/user/{userId}/coins/{coinCode}/trade-request")
    fun tradeBuyAsync(
        @Path("userId") userId: Int,
        @Path("coinCode") toCoin: String,
        @Body request: TradeBuyRequest
    ): Deferred<Response<ResponseBody>>

    @POST("user/{userId}/location")
    fun tradeSendUserLocationAsync(
        @Path("userId") userId: Int,
        @Body request: TradeLocationRequest
    ): Deferred<Response<ResponseBody>>

    @GET("user/{userId}/code/send")
    fun sendSmsCodeAsync(
        @Path("userId") userId: Int
    ): Deferred<Response<SendSmsCodeResponse>>

    @POST("user/{userId}/code/verify")
    fun verifySmsCodeAsync(
        @Path("userId") userId: Int,
        @Body verifySmsParam: VerifySmsCodeRequest
    ): Deferred<Response<ResponseBody>>

    @GET("user/{userId}/coins/{coinId}/transactions/utxo/{hex}")
    fun getUtxoListAsync(
        @Path("userId") userId: Int,
        @Path("coinId") coinId: String,
        @Path("hex") extendedPublicKey: String
    ): Deferred<Response<UtxoListResponse>>

    @GET("user/{userId}/coins/ETH/transactions/nonce")
    fun getEthereumNonceAsync(
        @Path("userId") userId: Int,
        @Path("address") toAddress: String
    ): Deferred<Response<EthereumResponse>>

    @GET("user/{userId}/coins/XRP/transactions/currentaccount")
    fun getRippleBlockHeaderAsync(
        @Path("userId") userId: Int
    ): Deferred<Response<RippleBlockResponse>>

    @GET("user/{userId}/coins/BNB/transactions/currentaccount")
    fun getBinanceBlockHeaderAsync(
        @Path("userId") userId: Int
    ): Deferred<Response<BinanceBlockResponse>>

    @GET("user/{userId}/coins/{coinId}/transactions/currentblock")
    fun getTronBlockHeaderAsync(
        @Path("userId") userId: Int,
        @Path("coinId") coinId: String
    ): Deferred<Response<TronBlockResponse>>
}