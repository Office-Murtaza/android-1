package com.belcobtm.domain.trade.list

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.PreferencesInteractor
import com.belcobtm.domain.trade.TradeRepository
import com.belcobtm.domain.trade.list.mapper.TradesDataToMyTradeMapper
import com.belcobtm.domain.trade.model.TradeHistoryDomainModel
import com.belcobtm.presentation.screens.wallet.trade.list.model.TradeItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveMyTradesUseCase(
    private val tradeRepository: TradeRepository,
    private val preferences: PreferencesInteractor,
    private val mapper: TradesDataToMyTradeMapper
) {

    operator fun invoke(): Flow<Either<Failure, List<TradeItem>>> =
        tradeRepository.observeTradeData()
            .map {
                when {
                    it.isRight ->
                        Either.Right(mapper.map((it as Either.Right<TradeHistoryDomainModel>).b, preferences.userId))
                    else ->
                        it as Either.Left<Failure>
                }
            }

}
