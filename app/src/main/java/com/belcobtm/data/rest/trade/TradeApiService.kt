package com.belcobtm.data.rest.trade

import android.location.Location
import com.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.belcobtm.data.model.trade.OrderStatus
import com.belcobtm.data.model.trade.TradeStatus
import com.belcobtm.data.rest.trade.request.CancelTradeRequest
import com.belcobtm.data.rest.trade.request.CreateOrderRequest
import com.belcobtm.data.rest.trade.request.CreateTradeRequest
import com.belcobtm.data.rest.trade.request.EditTradeRequest
import com.belcobtm.data.rest.trade.request.UpdateOrderRequest
import com.belcobtm.data.rest.trade.request.UserLocationRequest
import com.belcobtm.data.rest.trade.response.TradeItemResponse
import com.belcobtm.data.rest.trade.response.TradeOrderItemResponse
import com.belcobtm.data.rest.trade.response.TradesResponse
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.presentation.features.wallet.trade.create.model.CreateTradeItem
import com.belcobtm.presentation.features.wallet.trade.edit.EditTradeItem
import com.belcobtm.presentation.features.wallet.trade.order.create.model.TradeOrderItem

class TradeApiService(
    private val tradeApi: TradeApi,
    private val prefHelper: SharedPreferencesHelper
) {

    private companion object {

        const val VALIDATION_ERROR_REASON = 2
    }

    suspend fun loadTrades(): Either<Failure, TradesResponse> =
        withErrorHandling {
            val response = tradeApi.getTradesAsync(prefHelper.userId)
            response.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
        }

    suspend fun sendLocation(location: Location): Either<Failure, Unit> =
        withErrorHandling {
            val response =
                tradeApi.sendLocationAsync(
                    prefHelper.userId,
                    UserLocationRequest(location.latitude, location.longitude)
                )
            response.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
        }

    suspend fun createTrade(createTradeItem: CreateTradeItem, location: Location): Either<Failure, TradeItemResponse> =
        withErrorHandling {
            val request = with(createTradeItem) {
                CreateTradeRequest(
                    tradeType, coinCode, price, minLimit, maxLimit,
                    paymentOptions.joinToString(","), terms,
                    feePercent, fiatAmount,
                    location.longitude, location.latitude
                )
            }
            val response = tradeApi.createTradeAsync(prefHelper.userId, request)
            response.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
        }

    suspend fun editTrade(editTradeItem: EditTradeItem): Either<Failure, TradeItemResponse> =
        withErrorHandling {
            val request = with(editTradeItem) {
                EditTradeRequest(
                    tradeId, price, minAmount, maxAmount,
                    paymentOptions.joinToString(","), terms,
                    feePercent, fiatAmount
                )
            }
            val response = tradeApi.editTradeAsync(prefHelper.userId, request)
            response.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
        }

    suspend fun deleteTrade(tradeId: String): Either<Failure, TradeItemResponse> =
        withErrorHandling {
            val response = tradeApi.deleteTradeAsync(prefHelper.userId, tradeId)
            response.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
        }

    suspend fun cancelTrade(tradeId: String): Either<Failure, TradeItemResponse> =
        withErrorHandling {
            val request = CancelTradeRequest(tradeId, TradeStatus.CANCELLED)
            val response = tradeApi.cancelTradeAsync(prefHelper.userId, request)
            response.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
        }

    suspend fun deleteOrder(orderId: String): Either<Failure, TradeOrderItemResponse> =
        withErrorHandling {
            val response = tradeApi.deleteOrderAsync(prefHelper.userId, orderId)
            response.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
        }

    suspend fun createOrder(tradeOrder: TradeOrderItem, location: Location): Either<Failure, TradeOrderItemResponse> =
        withErrorHandling {
            val request = with(tradeOrder) {
                CreateOrderRequest(tradeId, price, cryptoAmount, fiatAmount, feePercent, location.longitude, location.latitude)
            }
            val response = tradeApi.createOrderAsync(prefHelper.userId, request)
            response.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
        }

    suspend fun updateOrder(
        orderId: String,
        @OrderStatus status: Int? = null,
        rate: Int? = null
    ): Either<Failure, TradeOrderItemResponse> =
        withErrorHandling {
            val request = UpdateOrderRequest(orderId, status, rate)
            val response = tradeApi.updateOrderAsync(prefHelper.userId, request)
            response.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
        }

    private inline fun <T> withErrorHandling(call: () -> Either<Failure, T>): Either<Failure, T> = try {
        call()
    } catch (messageError: Failure.MessageError) {
        Either.Left(
            if (messageError.code == VALIDATION_ERROR_REASON) {
                Failure.ValidationError(messageError.message)
            } else {
                Failure.ServerError()
            }
        )
    } catch (failure: Failure) {
        Either.Left(failure)
    }

}
