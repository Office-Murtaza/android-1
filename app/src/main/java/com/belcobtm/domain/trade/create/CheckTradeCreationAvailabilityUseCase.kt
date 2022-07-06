package com.belcobtm.domain.trade.create

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.PreferencesInteractor
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.trade.TradeRepository
import com.belcobtm.domain.trade.model.TradeHistoryDomainModel
import com.belcobtm.domain.trade.model.trade.TradeStatus
import com.belcobtm.domain.trade.model.trade.TradeType

class CheckTradeCreationAvailabilityUseCase(
    private val tradeRepository: TradeRepository,
    private val preferences: PreferencesInteractor
) : UseCase<Boolean, CheckTradeCreationAvailabilityUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Boolean> {
        val cacheData = tradeRepository.getTradeData()
        return if (cacheData?.isRight == true) {
            val userId = preferences.userId
            val trades = (cacheData as Either.Right<TradeHistoryDomainModel>).b
            val canCreateTrade = trades.trades.values.none {
                it.makerId == userId &&
                    it.type == params.tradeType &&
                    it.coin.name == params.coinCode &&
                    it.status != TradeStatus.DELETED
            }
            Either.Right(canCreateTrade)
        } else {
            cacheData as Either.Left<Failure>
        }
    }

    data class Params(
        val coinCode: String,
        val tradeType: TradeType
    )

}
