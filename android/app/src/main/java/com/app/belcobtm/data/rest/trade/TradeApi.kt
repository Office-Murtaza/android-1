package com.app.belcobtm.data.rest.trade

import com.app.belcobtm.data.rest.trade.request.*
import com.app.belcobtm.data.rest.trade.response.*
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.*

interface TradeApi {

    @GET("user/{userId}/trade-history")
    fun getTradesAsync(@Path("userId") userId: Int): Deferred<Response<TradesResponse>>

    @POST("user/{userId}/location")
    fun sendLocationAsync(
        @Path("userId") userId: Int,
        @Body body: UserLocationRequest
    ): Deferred<Response<Unit>>

    @POST("user/{userId}/trade")
    fun createTradeAsync(
        @Path("userId") userId: Int,
        @Body createTrade: CreateTradeRequest
    ): Deferred<Response<CreateTradeResponse>>

    @POST("user/{userId}/order")
    fun createOrderAsync(
        @Path("userId") userId: Int,
        @Body tradeOrder: CreateOrderRequest
    ): Deferred<Response<TradeOrderItemResponse>>

    @PUT("user/{userId}/order")
    fun updateOrderAsync(
        @Path("userId") userId: Int,
        @Body orderStatus: UpdateOrderRequest
    ): Deferred<Response<TradeOrderItemResponse>>

    @PUT("user/{userId}/trade")
    fun editTradeAsync(
        @Path("userId") userId: Int,
        @Body editTrade: EditTradeRequest
    ): Deferred<Response<EditTradeResponse>>

    @DELETE("user/{userId}/trade")
    fun deleteTradeAsync(
        @Path("userId") userId: Int,
        @Query("id") tradeId: String
    ): Deferred<Response<DeleteTradeResponse>>
}