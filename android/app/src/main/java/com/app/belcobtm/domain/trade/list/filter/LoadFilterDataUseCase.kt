package com.app.belcobtm.domain.trade.list.filter

import com.app.belcobtm.data.disk.database.AccountDao
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.trade.TradeRepository
import com.app.belcobtm.domain.trade.list.filter.mapper.TradeFilterItemMapper
import com.app.belcobtm.presentation.features.wallet.trade.list.filter.model.TradeFilterItem

class LoadFilterDataUseCase(
    private val tradeRepository: TradeRepository,
    private val accountDao: AccountDao,
    private val mapper: TradeFilterItemMapper
) : UseCase<TradeFilterItem, Unit>() {

    override suspend fun run(params: Unit): Either<Failure, TradeFilterItem> {
        val payments = tradeRepository.getAvailablePaymentOptions()
        val coins = accountDao.getItemList().orEmpty()
        val filter = tradeRepository.getFilter()
        return Either.Right(mapper.map(payments, coins, filter))
    }
}