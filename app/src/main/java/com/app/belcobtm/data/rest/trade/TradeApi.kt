package com.app.belcobtm.data.rest.trade

import com.app.belcobtm.data.rest.trade.request.*
import com.app.belcobtm.data.rest.trade.response.*
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.*

interface TradeApi {

    @GET("user/{userId}/trade-history")
    fun getTradesAsync(@Path("userId") userId: String): Deferred<Response<TradesResponse>>

    @POST("user/{userId}/location")
    fun sendLocationAsync(
        @Path("userId") userId: String,
        @Body body: UserLocationRequest
    ): Deferred<Response<Unit>>

    @POST("user/{userId}/trade")
    fun createTradeAsync(
        @Path("userId") userId: String,
        @Body createTrade: CreateTradeRequest
    ): Deferred<Response<CreateTradeResponse>>

    @POST("user/{userId}/order")
    fun createOrderAsync(
        @Path("userId") userId: String,
        @Body tradeOrder: CreateOrderRequest
    ): Deferred<Response<TradeOrderItemResponse>>

    @PUT("user/{userId}/order")
    fun updateOrderAsync(
        @Path("userId") userId: String,
        @Body orderStatus: UpdateOrderRequest
    ): Deferred<Response<TradeOrderItemResponse>>

    @PUT("user/{userId}/trade")
    fun editTradeAsync(
        @Path("userId") userId: String,
        @Body editTrade: EditTradeRequest
    ): Deferred<Response<EditTradeResponse>>

    @DELETE("user/{userId}/trade")
    fun deleteTradeAsync(
        @Path("userId") userId: String,
        @Query("id") tradeId: String
    ): Deferred<Response<DeleteTradeResponse>>

    @DELETE("user/{userId}/order")
    fun deleteOrderAsync(
        @Path("userId") userId: String,
        @Query("id") orderId: String
    ): Deferred<Response<TradeOrderItemResponse>>
}