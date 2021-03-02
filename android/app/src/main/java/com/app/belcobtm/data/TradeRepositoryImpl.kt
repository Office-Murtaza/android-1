package com.app.belcobtm.data

import com.app.belcobtm.data.inmemory.TradeInMemoryCache
import com.app.belcobtm.data.model.trade.TradeData
import com.app.belcobtm.data.rest.trade.TradeApiService
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.trade.TradeRepository
import com.app.belcobtm.presentation.features.wallet.trade.create.CreateTradeItem
import com.app.belcobtm.presentation.features.wallet.trade.edit.EditTradeItem
import kotlinx.coroutines.flow.Flow

class TradeRepositoryImpl(
    private val tradeApiService: TradeApiService,
    private val tradeInMemoryCache: TradeInMemoryCache
) : TradeRepository {

    override suspend fun fetchTrades() {
        val result = tradeApiService.loadTrades()
        tradeInMemoryCache.updateCache(result)
    }

    override fun observeTradeData(): Flow<Either<Failure, TradeData>?> =
        tradeInMemoryCache.data

    override suspend fun createTrade(createTradeItem: CreateTradeItem): Either<Failure, Unit> {
        val response = tradeApiService.createTrade(createTradeItem)
        return if (response.isRight) {
            Either.Right(Unit)
        } else {
            response as Either.Left
        }
    }

    override suspend fun editTrade(editTrade: EditTradeItem): Either<Failure, Unit> {
        val response = tradeApiService.editTrade(editTrade)
        return if (response.isRight) {
            Either.Right(Unit)
        } else {
            response as Either.Left
        }
    }

    override suspend fun deleteTrade(tradeId: Int): Either<Failure, Unit> {
        val response = tradeApiService.deleteTrade(tradeId)
        return if (response.isRight) {
            Either.Right(Unit)
        } else {
            response as Either.Left
        }
    }
}