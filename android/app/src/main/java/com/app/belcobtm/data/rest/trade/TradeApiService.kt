package com.app.belcobtm.data.rest.trade

import android.location.Location
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.model.trade.OrderStatus
import com.app.belcobtm.data.rest.trade.request.*
import com.app.belcobtm.data.rest.trade.response.*
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.features.wallet.trade.create.model.CreateTradeItem
import com.app.belcobtm.presentation.features.wallet.trade.edit.EditTradeItem
import com.app.belcobtm.presentation.features.wallet.trade.order.create.model.TradeOrderItem

class TradeApiService(
    private val tradeApi: TradeApi,
    private val prefHelper: SharedPreferencesHelper
) {

    private companion object {
        const val VALIDATION_ERROR_REASON = 2
    }

    suspend fun loadTrades(): Either<Failure, TradesResponse> =
        withErrorHandling {
            val response = tradeApi.getTradesAsync(prefHelper.userId).await()
            response.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
        }


    suspend fun sendLocation(location: Location): Either<Failure, Unit> =
        withErrorHandling {
            val response =
                tradeApi.sendLocation(
                    prefHelper.userId,
                    UserLocationRequest(location.latitude, location.longitude)
                ).await()
            response.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
        }

    suspend fun createTrade(
        createTradeItem: CreateTradeItem,
        location: Location?
    ): Either<Failure, CreateTradeResponse> =
        withErrorHandling {
            val request = with(createTradeItem) {
                CreateTradeRequest(
                    tradeType, coinCode, price, minLimit, maxLimit,
                    paymentOptions.joinToString(","), terms,
                    location?.latitude, location?.longitude
                )
            }
            val response = tradeApi.createTradeAsync(prefHelper.userId, request).await()
            response.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
        }

    suspend fun editTrade(editTradeItem: EditTradeItem): Either<Failure, EditTradeResponse> =
        withErrorHandling {
            val request = with(editTradeItem) {
                EditTradeRequest(
                    tradeId, price, minAmount, maxAmount,
                    paymentOptions.joinToString(","), terms
                )
            }
            val response = tradeApi.editTradeAsync(prefHelper.userId, request).await()
            response.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
        }

    suspend fun deleteTrade(tradeId: String): Either<Failure, DeleteTradeResponse> =
        withErrorHandling {
            val response = tradeApi.deleteTradeAsync(prefHelper.userId, tradeId).await()
            response.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
        }

    suspend fun createOrder(tradeOrder: TradeOrderItem): Either<Failure, TradeOrderItemResponse> =
        withErrorHandling {
            val request = with(tradeOrder) {
                CreateOrderRequest(tradeId, price, cryptoAmount, fiatAmount, terms)
            }
            val response = tradeApi.createOrderAsync(prefHelper.userId, request).await()
            response.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
        }

    suspend fun updateOrder(
        orderId: String,
        @OrderStatus status: Int? = null,
        rate: Int? = null
    ): Either<Failure, TradeOrderItemResponse> =
        withErrorHandling {
            val request = UpdateOrderRequest(orderId, status, rate)
            val response = tradeApi.updateOrderAsync(prefHelper.userId, request).await()
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