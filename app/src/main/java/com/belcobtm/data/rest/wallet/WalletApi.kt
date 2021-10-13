package com.belcobtm.data.rest.wallet

import com.belcobtm.data.rest.wallet.request.PriceChartPeriod
import com.belcobtm.data.rest.wallet.response.BalanceResponse
import com.belcobtm.data.rest.wallet.response.ChartResponse
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WalletApi {

    @GET("user/{userId}/balance")
    fun getBalanceAsync(
        @Path("userId") userId: String,
        @Query("coins") coins: List<String>
    ): Deferred<Response<BalanceResponse>>

    @GET("coin/{coinId}/price-chart")
    fun getChartAsync(
        @Path("coinId") coinCode: String,
        @Query("period") @PriceChartPeriod period: Int
    ): Deferred<Response<ChartResponse>>

    @GET("user/{userId}/coin/{coinId}/switch")
    fun toggleCoinStateAsync(
        @Path("userId") userId: String,
        @Path("coinId") coinCode: String,
        @Query("enabled") enabled: Boolean
    ): Deferred<Response<Unit>>
}