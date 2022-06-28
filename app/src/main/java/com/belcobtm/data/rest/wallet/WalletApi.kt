package com.belcobtm.data.rest.wallet

import com.belcobtm.data.rest.wallet.request.PriceChartPeriod
import com.belcobtm.data.rest.wallet.response.ChartResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WalletApi {

    @GET("coin/{coinId}/price-chart")
    suspend fun getChartAsync(
        @Path("coinId") coinCode: String,
        @Query("period") period: PriceChartPeriod
    ): Response<ChartResponse>

    @GET("user/{userId}/coin/{coinId}/switch")
    suspend fun toggleCoinStateAsync(
        @Path("userId") userId: String,
        @Path("coinId") coinCode: String,
        @Query("enabled") enabled: Boolean
    ): Response<Unit>

}
