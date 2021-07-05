package com.belcobtm.domain.wallet

import com.belcobtm.data.rest.wallet.request.PriceChartPeriod
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.wallet.item.ChartDataItem
import com.belcobtm.domain.wallet.item.CoinDataItem

interface WalletRepository {

    suspend fun getCoinItemByCode(coinCode: String): Either<Failure, CoinDataItem>

    suspend fun getCoinItemList(): Either<Failure, List<CoinDataItem>>

    suspend fun getChart(
        coinCode: String,
        @PriceChartPeriod period: Int
    ): Either<Failure, ChartDataItem>
}
