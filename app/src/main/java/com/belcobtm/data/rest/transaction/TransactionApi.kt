package com.belcobtm.data.rest.transaction

import com.belcobtm.data.rest.transaction.request.CoinToCoinExchangeRequest
import com.belcobtm.data.rest.transaction.request.SellPreSubmitRequest
import com.belcobtm.data.rest.transaction.request.SellRequest
import com.belcobtm.data.rest.transaction.request.SendGiftRequest
import com.belcobtm.data.rest.transaction.request.StakeRequest
import com.belcobtm.data.rest.transaction.request.TradeRecallRequest
import com.belcobtm.data.rest.transaction.request.TradeReserveRequest
import com.belcobtm.data.rest.transaction.request.WithdrawRequest
import com.belcobtm.data.rest.transaction.response.GetGiftAddressResponse
import com.belcobtm.data.rest.transaction.response.GetTransactionsResponse
import com.belcobtm.data.rest.transaction.response.ReceiverAccountActivatedResponse
import com.belcobtm.data.rest.transaction.response.SellPreSubmitResponse
import com.belcobtm.data.rest.transaction.response.StakeDetailsResponse
import com.belcobtm.data.rest.transaction.response.TransactionDetailsResponse
import com.belcobtm.data.rest.transaction.response.TransactionPlanResponse
import com.belcobtm.data.rest.transaction.response.hash.UtxoListResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TransactionApi {

    @GET("user/{userId}/coin/{coinCode}/transaction-plan")
    suspend fun getTransactionPlanAsync(
        @Path("userId") userId: String,
        @Path("coinCode") coinCode: String
    ): Response<TransactionPlanResponse>

    @GET("user/{userId}/coin/{coinCode}/transaction-history")
    suspend fun getTransactionsAsync(
        @Path("userId") userId: String,
        @Path("coinCode") coinCode: String
    ): Response<GetTransactionsResponse>

    @GET("coin/{coinCode}/receiver-account-activated")
    suspend fun receiverAccountActivatedAsync(
        @Path("coinCode") coinCode: String,
        @Query("toAddress") toAddress: String
    ): Response<ReceiverAccountActivatedResponse>

    @POST("user/{userId}/coin/{coinCode}/submit")
    suspend fun withdrawAsync(
        @Path("userId") userId: String,
        @Path("coinCode") coinCode: String,
        @Body body: WithdrawRequest
    ): Response<TransactionDetailsResponse>

    @GET("coin/{coinCode}/transfer-address")
    suspend fun getGiftAddressAsync(
        @Path("coinCode") coinCode: String,
        @Query("phone") phone: String?
    ): Response<GetGiftAddressResponse>

    @POST("user/{userId}/coin/{coinCode}/submit")
    suspend fun sendGiftAsync(
        @Path("userId") userId: String,
        @Path("coinCode") coinCode: String,
        @Body body: SendGiftRequest
    ): Response<TransactionDetailsResponse>

    @POST("user/{userId}/coin/{coinCode}/pre-submit")
    suspend fun sellPreSubmitAsync(
        @Path("userId") userId: String,
        @Path("coinCode") coinCode: String,
        @Body body: SellPreSubmitRequest
    ): Response<SellPreSubmitResponse>

    @POST("user/{userId}/coin/{coinCode}/submit")
    suspend fun sellAsync(
        @Path("userId") userId: String,
        @Path("coinCode") coinCode: String,
        @Body body: SellRequest
    ): Response<TransactionDetailsResponse>

    @POST("user/{userId}/coin/{coinCode}/submit")
    suspend fun exchangeAsync(
        @Path("userId") userId: String,
        @Path("coinCode") coinFrom: String,
        @Body request: CoinToCoinExchangeRequest
    ): Response<TransactionDetailsResponse>

    @GET("coin/{coinCode}/utxo")
    suspend fun getUtxoListAsync(
        @Path("coinCode") coinCode: String,
        @Query("xpub") extendedPublicKey: String
    ): Response<UtxoListResponse>

    @POST("user/{userId}/coin/{coinCode}/submit")
    suspend fun submitRecallAsync(
        @Path("userId") userId: String,
        @Path("coinCode") coinCode: String,
        @Body body: TradeRecallRequest
    ): Response<TransactionDetailsResponse>

    @POST("user/{userId}/coin/{coinCode}/submit")
    suspend fun submitReserveAsync(
        @Path("userId") userId: String,
        @Path("coinCode") coinCode: String,
        @Body body: TradeReserveRequest
    ): Response<TransactionDetailsResponse>

    @GET("user/{userId}/coin/{coinCode}/staking-details")
    suspend fun stakeDetailsAsync(
        @Path("userId") userId: String,
        @Path("coinCode") coinCode: String
    ): Response<StakeDetailsResponse>

    @POST("user/{userId}/coin/{coinCode}/submit")
    suspend fun stakeOrUnStakeAsync(
        @Path("userId") userId: String,
        @Path("coinCode") coinCode: String,
        @Body body: StakeRequest
    ): Response<TransactionDetailsResponse>

}
