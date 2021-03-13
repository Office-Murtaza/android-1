package com.app.belcobtm.data.rest.trade

import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.rest.trade.request.TradeRequest
import com.app.belcobtm.data.rest.trade.response.CreateTradeResponse
import com.app.belcobtm.data.rest.trade.response.DeleteTradeResponse
import com.app.belcobtm.data.rest.trade.response.EditTradeResponse
import com.app.belcobtm.data.rest.trade.response.TradesResponse
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.features.wallet.trade.create.model.CreateTradeItem
import com.app.belcobtm.presentation.features.wallet.trade.edit.EditTradeItem

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

    suspend fun createTrade(createTradeItem: CreateTradeItem): Either<Failure, CreateTradeResponse> =
        withErrorHandling {
            val request = with(createTradeItem) {
                TradeRequest(
                    tradeType, coinCode, price, minLimit, maxLimit,
                    paymentOptions.joinToString(","), terms
                )
            }
            val response = tradeApi.createTradeAsync(prefHelper.userId, request).await()
            response.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
        }

    suspend fun editTrade(editTradeItem: EditTradeItem): Either<Failure, EditTradeResponse> =
        withErrorHandling {
            val request = TradeRequest(
                0, "", 0, 0, 0, "", "", 0.0, 0.0
            )
            val response = tradeApi.editTradeAsync(prefHelper.userId, request).await()
            response.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
        }

    suspend fun deleteTrade(tradeId: Int): Either<Failure, DeleteTradeResponse> =
        withErrorHandling {
            val response = tradeApi.deleteTradeAsync(prefHelper.userId, tradeId).await()
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