package com.app.belcobtm.domain.trade.create

import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.model.trade.TradeData
import com.app.belcobtm.data.model.trade.TradeType
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.trade.TradeRepository

class CheckTradeCreationAvailabilityUseCase(
    private val tradeRepository: TradeRepository,
    private val sharedPreferencesHelper: SharedPreferencesHelper
) : UseCase<Boolean, CheckTradeCreationAvailabilityUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Boolean> {
        val cacheData = tradeRepository.getTradeData()
        return if (cacheData?.isRight == true) {
            val userId = sharedPreferencesHelper.userId
            val trades = (cacheData as Either.Right<TradeData>).b
            val canCreateTrade = trades.trades.none {
                it.makerId == userId && it.type == params.tradeType && it.coinCode == params.coinCode
            }
            Either.Right(canCreateTrade)
        } else {
            cacheData as Either.Left<Failure>
        }
    }

    data class Params(
        val coinCode: String,
        @TradeType val tradeType: Int
    )

}