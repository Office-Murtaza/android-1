package com.app.belcobtm.data.rest.trade

import com.app.belcobtm.data.rest.trade.request.CreateTradeRequest
import com.app.belcobtm.data.rest.trade.request.EditTradeRequest
import com.app.belcobtm.data.rest.trade.response.CreateTradeResponse
import com.app.belcobtm.data.rest.trade.response.DeleteTradeResponse
import com.app.belcobtm.data.rest.trade.response.EditTradeResponse
import com.app.belcobtm.data.rest.trade.response.TradesResponse
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.*

interface TradeApi {

    @GET("user/{userId}/trades")
    fun getTradesAsync(@Path("userId") userId: Int): Deferred<Response<TradesResponse>>

    @POST("user/{userId}/trade")
    fun createTradeAsync(
        @Path("userId") userId: Int,
        @Body createTrade: CreateTradeRequest
    ): Deferred<Response<CreateTradeResponse>>

    @PUT("user/{userId}/trade")
    fun editTradeAsync(
        @Path("userId") userId: Int,
        @Body editTrade: EditTradeRequest
    ): Deferred<Response<EditTradeResponse>>

    @DELETE("user/{userId}/trade")
    fun deleteTradeAsync(
        @Path("userId") userId: Int,
        @Query("id") tradeId: Int
    ): Deferred<Response<DeleteTradeResponse>>
}