package com.app.belcobtm.domain.trade

import com.app.belcobtm.data.model.trade.TradeData
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.features.wallet.trade.create.CreateTradeItem
import com.app.belcobtm.presentation.features.wallet.trade.edit.EditTradeItem
import kotlinx.coroutines.flow.Flow

interface TradeRepository {

    suspend fun fetchTrades()

    fun observeTradeData(): Flow<Either<Failure, TradeData>?>

    suspend fun createTrade(createTradeItem: CreateTradeItem): Either<Failure, Unit>

    suspend fun editTrade(editTrade: EditTradeItem): Either<Failure, Unit>

    suspend fun deleteTrade(tradeId: Int): Either<Failure, Unit>

}