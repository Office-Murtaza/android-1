package com.app.belcobtm.data

import com.app.belcobtm.data.disk.database.wallet.FullCoinEntity
import com.app.belcobtm.data.disk.database.wallet.WalletDao
import com.app.belcobtm.data.disk.database.wallet.toDataItem
import com.app.belcobtm.data.rest.wallet.WalletApiService
import com.app.belcobtm.data.rest.wallet.request.PriceChartPeriod
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.WalletRepository
import com.app.belcobtm.domain.wallet.item.ChartDataItem
import com.app.belcobtm.domain.wallet.item.CoinDataItem

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
        @PriceChartPeriod period: Int
    ): Either<Failure, ChartDataItem> = apiService.getChart(coinCode, period)

}
