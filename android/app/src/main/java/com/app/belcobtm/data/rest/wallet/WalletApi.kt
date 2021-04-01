package com.app.belcobtm.data.rest.wallet

import com.app.belcobtm.data.rest.wallet.request.PriceChartPeriod
import com.app.belcobtm.data.rest.wallet.response.BalanceResponse
import com.app.belcobtm.data.rest.wallet.response.ChartResponse
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WalletApi {

    @GET("user/{userId}/balance")
    fun getBalanceAsync(
        @Path("userId") userId: Int,
        @Query("coins") coins: List<String>
    ): Deferred<Response<BalanceResponse>>

    @GET("coin/{coinId}/price-chart")
    fun getChartAsync(
        @Path("coinId") coinCode: String,
        @Query("period") @PriceChartPeriod period: Int
    ): Deferred<Response<ChartResponse>>

    @GET("user/{userId}/coin/{coinId}/manage")
    fun toggleCoinStateAsync(
        @Path("userId") userId: Int,
        @Path("coinId") coinCode: String,
        @Query("enabled") enabled: Boolean
    ): Deferred<Response<Unit>>
}