package com.belcobtm.data.rest.trade

import android.location.Location
import com.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.belcobtm.data.rest.trade.request.CancelTradeRequest
import com.belcobtm.data.rest.trade.request.CreateOrderRequest
import com.belcobtm.data.rest.trade.request.CreateTradeRequest
import com.belcobtm.data.rest.trade.request.EditTradeRequest
import com.belcobtm.data.rest.trade.request.UpdateOrderRequest
import com.belcobtm.data.rest.trade.request.UserLocationRequest
import com.belcobtm.data.rest.trade.response.TradeHistoryResponse
import com.belcobtm.data.rest.trade.response.TradeOrderResponse
import com.belcobtm.data.rest.trade.response.TradeResponse
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.trade.model.CreateTradeDomainModel
import com.belcobtm.domain.trade.model.order.OrderStatus
import com.belcobtm.domain.trade.model.trade.TradeStatus
import com.belcobtm.presentation.screens.wallet.trade.edit.EditTradeItem
import com.belcobtm.presentation.screens.wallet.trade.order.create.model.TradeOrderItem

class TradeApiService(
    private val tradeApi: TradeApi,
    private val prefHelper: SharedPreferencesHelper
) {

    suspend fun loadTrades(): Either<Failure, TradeHistoryResponse> =
        withErrorHandling {
            val response = tradeApi.getTradesAsync(prefHelper.userId)
            response.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
        }

    suspend fun sendLocation(location: Location): Either<Failure, Unit> =
        withErrorHandling {
            val response =
                tradeApi.sendLocationAsync(
                    prefHelper.userId,
                    UserLocationRequest(
                        location.latitude,
                        location.longitude
                    )
                )
            response.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
        }

    suspend fun createTrade(createTradeItem: CreateTradeDomainModel, location: Location): Either<Failure, TradeResponse> =
        withErrorHandling {
            val request = with(createTradeItem) {
                CreateTradeRequest(
                    type = tradeType.name,
                    coin = coinCode,
                    price = price,
                    minLimit = minLimit,
                    maxLimit = maxLimit,
                    fiatAmount = fiatAmount,
                    feePercent = feePercent,
                    paymentMethods = paymentOptions.map { it.name },
                    terms = terms,
                    longitude = location.longitude,
                    latitude = location.latitude
                )
            }
            val response = tradeApi.createTradeAsync(prefHelper.userId, request)
            response.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
        }

    suspend fun editTrade(editTradeItem: EditTradeItem): Either<Failure, TradeResponse> =
        withErrorHandling {
            val request = with(editTradeItem) {
                EditTradeRequest(
                    id = tradeId,
                    price = price,
                    minLimit = minAmount,
                    maxLimit = maxAmount,
                    paymentMethods = paymentOptions.map { it.name },
                    terms = terms,
                    feePercent = feePercent,
                    fiatAmount = fiatAmount
                )
            }
            val response = tradeApi.editTradeAsync(prefHelper.userId, request)
            response.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
        }

    suspend fun cancelTrade(tradeId: String): Either<Failure, TradeResponse> =
        withErrorHandling {
            val request = CancelTradeRequest(tradeId, TradeStatus.CANCELED.name)
            val response = tradeApi.cancelTradeAsync(prefHelper.userId, request)
            response.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
        }

    suspend fun deleteTrade(tradeId: String): Either<Failure, TradeResponse> =
        withErrorHandling {
            val response = tradeApi.deleteTradeAsync(prefHelper.userId, tradeId)
            response.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
        }

    suspend fun createOrder(tradeOrder: TradeOrderItem, location: Location): Either<Failure, TradeOrderResponse> =
        withErrorHandling {
            val request = with(tradeOrder) {
                CreateOrderRequest(
                    tradeId,
                    price,
                    cryptoAmount,
                    fiatAmount,
                    feePercent,
                    location.longitude,
                    location.latitude
                )
            }
            val response = tradeApi.createOrderAsync(prefHelper.userId, request)
            response.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
        }

    suspend fun updateOrder(
        orderId: String,
        status: OrderStatus? = null,
        rate: Int? = null
    ): Either<Failure, TradeOrderResponse> =
        withErrorHandling {
            val request = UpdateOrderRequest(orderId, status?.name, rate)
            val response = tradeApi.updateOrderAsync(prefHelper.userId, request)
            response.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
        }

    suspend fun deleteOrder(orderId: String): Either<Failure, TradeOrderResponse> =
        withErrorHandling {
            val response = tradeApi.deleteOrderAsync(prefHelper.userId, orderId)
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

    private companion object {

        const val VALIDATION_ERROR_REASON = 2
    }

}
