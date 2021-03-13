package com.app.belcobtm.data.inmemory

import com.app.belcobtm.data.mapper.TradesResponseToTradeDataMapper
import com.app.belcobtm.data.model.trade.TradeData
import com.app.belcobtm.data.model.trade.filter.TradeFilter
import com.app.belcobtm.data.rest.trade.response.TradesResponse
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TradeInMemoryCache(
    private val tradesMapper: TradesResponseToTradeDataMapper
) {

    companion object {
        val UNDEFINED_DISTANCE = Double.MAX_VALUE
    }

    private val cache = MutableStateFlow<Either<Failure, TradeData>?>(null)
    private val tradeFilter = MutableStateFlow<TradeFilter?>(null)

    val observableData: StateFlow<Either<Failure, TradeData>?>
        get() = cache

    val data: Either<Failure, TradeData>?
        get() = cache.value

    val observableFilter: StateFlow<TradeFilter?>
        get() = tradeFilter

    val filter: TradeFilter?
        get() = tradeFilter.value

    fun updateCache(response: Either<Failure, TradesResponse>) {
        if (response.isLeft) {
            cache.value = response as Either.Left<Failure>
        } else {
            cache.value = Either.Right(tradesMapper.map((response as Either.Right<TradesResponse>).b))
        }
    }

    fun updateFilter(filter: TradeFilter) {
        tradeFilter.value = filter
    }

    fun updateDistances(distances: Map<Int, Double>) {
        val currentCache = cache.value
        val tradeData: TradeData =
            (currentCache?.takeIf { currentCache.isRight } as? Either.Right<TradeData>)?.b ?: return
        cache.value = Either.Right(tradeData.copy(
            trades = tradeData.trades.asSequence()
                .map { it.copy(distance = distances[it.id] ?: UNDEFINED_DISTANCE) }
                .sortedBy { it.distance }
                .toList()
        ))
    }

    fun cleanCache() {
        cache.value = null
    }

}