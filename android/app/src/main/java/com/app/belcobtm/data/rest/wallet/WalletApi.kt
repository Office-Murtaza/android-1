package com.app.belcobtm.data.rest.wallet

import com.app.belcobtm.data.rest.wallet.request.CoinToCoinExchangeRequest
import com.app.belcobtm.data.rest.wallet.request.VerifySmsCodeRequest
import com.app.belcobtm.data.rest.wallet.response.SendSmsCodeResponse
import com.app.belcobtm.data.rest.wallet.response.hash.*
import kotlinx.coroutines.Deferred
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface WalletApi {
    @POST("user/{userId}/coins/{coinCode}/transactions/submit")
    fun coinToCoinExchangeAsync(
        @Path("userId") userId: Int,
        @Path("coinCode") coinFrom: String,
        @Body request: CoinToCoinExchangeRequest
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
        @Path("userId") userId: Int
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
    ):  Deferred<Response<TronBlockResponse>>
}