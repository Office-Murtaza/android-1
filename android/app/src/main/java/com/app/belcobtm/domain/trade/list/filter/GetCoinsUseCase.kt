package com.app.belcobtm.domain.trade.list.filter

import com.app.belcobtm.data.disk.database.AccountDao
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.trade.list.filter.mapper.CoinCodeMapper
import com.app.belcobtm.presentation.features.wallet.trade.list.filter.model.CoinCodeListItem

class GetCoinsUseCase(
    private val coinCodeMapper: CoinCodeMapper,
    private val accountDao: AccountDao
) : UseCase<List<CoinCodeListItem>, Unit>() {

    override suspend fun run(params: Unit): Either<Failure, List<CoinCodeListItem>> =
        Either.Right(accountDao.getItemList().orEmpty().map(coinCodeMapper::map))
}