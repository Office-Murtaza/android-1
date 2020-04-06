package com.app.belcobtm.data.rest.wallet

import com.app.belcobtm.data.rest.wallet.request.CoinToCoinExchangeRequest
import kotlinx.coroutines.Deferred
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface WalletApi {
    @POST("user/{userId}/coins/{coinCode}/transactions/submit")
    fun coinToCoinExchangeAsync(
        @Path("userId") userId: Int,
        @Path("coinCode") coinFrom: String,
        @Body request: CoinToCoinExchangeRequest
    ): Deferred<Response<ResponseBody>>
}