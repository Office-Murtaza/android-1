package com.belcobtm.data.rest.transaction

import com.belcobtm.data.rest.transaction.request.*
import com.belcobtm.data.rest.transaction.response.*
import com.belcobtm.data.rest.transaction.response.hash.*
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.*

interface TransactionApi {

    @GET("user/{userId}/coin/{coinCode}/transaction-plan")
    fun getTransactionPlanAsync(
        @Path("userId") userId: String,
        @Path("coinCode") coinCode: String
    ): Deferred<Response<TransactionPlanResponse>>

    @GET("user/{userId}/coin/{coinCode}/transaction-history")
    fun getTransactionsAsync(
        @Path("userId") userId: String,
        @Path("coinCode") coinCode: String
    ): Deferred<Response<GetTransactionsResponse>>

    @GET("coin/{coinCode}/receiver-account-activated")
    fun receiverAccountActivatedAsync(
        @Path("coinCode") coinCode: String,
        @Query("toAddress") toAddress: String
    ): Deferred<Response<ReceiverAccountActivatedResponse>>

    @POST("user/{userId}/coin/{coinCode}/submit")
    fun withdrawAsync(
        @Path("userId") userId: String,
        @Path("coinCode") coinCode: String,
        @Body body: WithdrawRequest
    ): Deferred<Response<TransactionDetailsResponse>>

    @GET("coin/{coinCode}/transfer-address")
    fun getGiftAddressAsync(
        @Path("coinCode") coinCode: String,
        @Query("phone") phone: String?
    ): Deferred<Response<GetGiftAddressResponse>>

    @POST("user/{userId}/coin/{coinCode}/submit")
    fun sendGiftAsync(
        @Path("userId") userId: String,
        @Path("coinCode") coinCode: String,
        @Body body: SendGiftRequest
    ): Deferred<Response<TransactionDetailsResponse>>

    @GET("user/{userId}/limits")
    fun sellGetLimitsAsync(
        @Path("userId") userId: String
    ): Deferred<Response<LimitsResponse>>

    @POST("user/{userId}/coin/{coinCode}/pre-submit")
    fun sellPreSubmitAsync(
        @Path("userId") userId: String,
        @Path("coinCode") coinCode: String,
        @Body body: SellPreSubmitRequest
    ): Deferred<Response<SellPreSubmitResponse>>

    @POST("user/{userId}/coin/{coinCode}/submit")
    fun sellAsync(
        @Path("userId") userId: String,
        @Path("coinCode") coinCode: String,
        @Body body: SellRequest
    ): Deferred<Response<TransactionDetailsResponse>>

    @POST("user/{userId}/coin/{coinCode}/submit")
    fun exchangeAsync(
        @Path("userId") userId: String,
        @Path("coinCode") coinFrom: String,
        @Body request: CoinToCoinExchangeRequest
    ): Deferred<Response<TransactionDetailsResponse>>

    @GET("coin/{coinCode}/utxo")
    fun getUtxoListAsync(
        @Path("coinCode") coinCode: String,
        @Query("xpub") extendedPublicKey: String
    ): Deferred<Response<UtxoListResponse>>

    @GET("coin/nonce")
    fun getEthereumNonceAsync(
        @Query("address") toAddress: String
    ): Deferred<Response<EthereumResponse>>

    @GET("coin/XRP/current-account")
    fun getRippleBlockHeaderAsync(
        @Query("address") address: String
    ): Deferred<Response<RippleBlockResponse>>

    @GET("coin/current-account-activated")
    fun checkRippleAccountActivationAsync(
        @Query("address") address: String
    ): Deferred<Response<XRPAccountActivatedResponse>>

    @GET("coin/BNB/current-account")
    fun getBinanceBlockHeaderAsync(
        @Query("address") address: String
    ): Deferred<Response<BinanceBlockResponse>>

    @GET("coin/current-block")
    fun getTronBlockHeaderAsync(): Deferred<Response<TronBlockResponse>>

    @POST("user/{userId}/coin/{coinCode}/submit")
    fun submitRecallAsync(
        @Path("userId") userId: String,
        @Path("coinCode") coinCode: String,
        @Body body: TradeRecallRequest
    ): Deferred<Response<TransactionDetailsResponse>>

    @POST("user/{userId}/coin/{coinCode}/submit")
    fun submitReserveAsync(
        @Path("userId") userId: String,
        @Path("coinCode") coinCode: String,
        @Body body: TradeReserveRequest
    ): Deferred<Response<TransactionDetailsResponse>>

    @GET("user/{userId}/coin/{coinCode}/staking-details")
    fun stakeDetailsAsync(
        @Path("userId") userId: String,
        @Path("coinCode") coinCode: String
    ): Deferred<Response<StakeDetailsResponse>>

    @POST("user/{userId}/coin/{coinCode}/submit")
    fun stakeOrUnStakeAsync(
        @Path("userId") userId: String,
        @Path("coinCode") coinCode: String,
        @Body body: StakeRequest
    ): Deferred<Response<TransactionDetailsResponse>>
}