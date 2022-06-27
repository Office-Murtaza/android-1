package com.belcobtm.data

import com.belcobtm.data.disk.database.wallet.FullCoinEntity
import com.belcobtm.data.disk.database.wallet.WalletDao
import com.belcobtm.data.disk.database.wallet.toDataItem
import com.belcobtm.data.rest.wallet.WalletApiService
import com.belcobtm.data.rest.wallet.request.PriceChartPeriod
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.wallet.WalletRepository
import com.belcobtm.domain.wallet.item.ChartDataItem
import com.belcobtm.domain.wallet.item.CoinDataItem

class WalletRepositoryImpl(
    private val walletDao: WalletDao,
    private val apiService: WalletApiService
) : WalletRepository {

    override suspend fun getCoinItemByCode(
        coinCode: String
    ): Either<Failure, CoinDataItem> =
        Either.Right(walletDao.getCoinByCode(coinCode).toDataItem())

    override suspend fun getCoinItemList(): Either<Failure, List<CoinDataItem>> =
        Either.Right(walletDao.getCoins().map(FullCoinEntity::toDataItem))

    override suspend fun getChart(
        coinCode: String,
        period: PriceChartPeriod
    ): Either<Failure, ChartDataItem> = apiService.getChart(coinCode, period)

    override suspend fun getTotalBalance(): Either<Failure, Double> =
        Either.Right(walletDao.getTotalBalance())

    override suspend fun updateBalance(
        coinCode: String,
        newBalance: Double,
        newBalanceUsd: Double,
        newTotal: Double
    ): Either<Failure, Unit> {
        walletDao.updateBalance(coinCode, newBalanceUsd, newBalance)
        walletDao.updateTotalBalance(newTotal)
        return Either.Right(Unit)
    }

    override suspend fun updateReservedBalance(
        coinCode: String,
        newBalance: Double,
        newBalanceUsd: Double,
        newTotal: Double
    ): Either<Failure, Unit> {
        walletDao.updateReservedBalance(coinCode, newBalanceUsd, newBalance)
        walletDao.updateTotalBalance(newTotal)
        return Either.Right(Unit)
    }
}
