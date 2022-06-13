package com.belcobtm.data.rest.trade

import com.belcobtm.data.rest.trade.request.CancelTradeRequest
import com.belcobtm.data.rest.trade.request.CreateOrderRequest
import com.belcobtm.data.rest.trade.request.CreateTradeRequest
import com.belcobtm.data.rest.trade.request.EditTradeRequest
import com.belcobtm.data.rest.trade.request.UpdateOrderRequest
import com.belcobtm.data.rest.trade.request.UserLocationRequest
import com.belcobtm.data.rest.trade.response.TradeItemResponse
import com.belcobtm.data.rest.trade.response.TradeOrderItemResponse
import com.belcobtm.data.rest.trade.response.TradesResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface TradeApi {

    @GET("user/{userId}/trade-history")
    suspend fun getTradesAsync(
        @Path("userId") userId: String
    ): Response<TradesResponse>

    @POST("user/{userId}/location")
    suspend fun sendLocationAsync(
        @Path("userId") userId: String,
        @Body body: UserLocationRequest
    ): Response<Unit>

    @POST("user/{userId}/trade")
    suspend fun createTradeAsync(
        @Path("userId") userId: String,
        @Body createTrade: CreateTradeRequest
    ): Response<TradeItemResponse>

    @PUT("user/{userId}/trade")
    suspend fun editTradeAsync(
        @Path("userId") userId: String,
        @Body editTrade: EditTradeRequest
    ): Response<TradeItemResponse>

    @PUT("user/{userId}/trade")
    suspend fun cancelTradeAsync(
        @Path("userId") userId: String,
        @Body cancelTrade: CancelTradeRequest
    ): Response<TradeItemResponse>

    @DELETE("user/{userId}/trade")
    suspend fun deleteTradeAsync(
        @Path("userId") userId: String,
        @Query("id") tradeId: String
    ): Response<TradeItemResponse>

    @POST("user/{userId}/order")
    suspend fun createOrderAsync(
        @Path("userId") userId: String,
        @Body tradeOrder: CreateOrderRequest
    ): Response<TradeOrderItemResponse>

    @PUT("user/{userId}/order")
    suspend fun updateOrderAsync(
        @Path("userId") userId: String,
        @Body orderStatus: UpdateOrderRequest
    ): Response<TradeOrderItemResponse>

    @DELETE("user/{userId}/order")
    suspend fun deleteOrderAsync(
        @Path("userId") userId: String,
        @Query("id") orderId: String
    ): Response<TradeOrderItemResponse>

}
