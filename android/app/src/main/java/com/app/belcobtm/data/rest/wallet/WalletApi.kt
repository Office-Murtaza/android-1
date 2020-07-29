package com.app.belcobtm.data.rest.wallet

import com.app.belcobtm.data.rest.wallet.response.BalanceResponse
import com.app.belcobtm.data.rest.wallet.response.ChartResponse
import com.app.belcobtm.data.rest.wallet.response.GetCoinFeeResponse
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

    @GET("user/{userId}/coin/{coinId}/price-chart")
    fun getChartAsync(
        @Path("userId") userId: Int,
        @Path("coinId") coinCode: String
    ): Deferred<Response<ChartResponse>>

    @GET("coin/{coinId}/settings")
    fun getCoinFeeAsync(
        @Path("coinId") coinCode: String
    ): Deferred<Response<GetCoinFeeResponse>>
}