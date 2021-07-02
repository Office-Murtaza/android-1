package com.app.belcobtm.domain.wallet

import com.app.belcobtm.data.rest.wallet.request.PriceChartPeriod
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.item.ChartDataItem
import com.app.belcobtm.domain.wallet.item.CoinDataItem

interface WalletRepository {

    suspend fun getCoinItemByCode(coinCode: String): Either<Failure, CoinDataItem>

    suspend fun getCoinItemList(): Either<Failure, List<CoinDataItem>>

    suspend fun getChart(
        coinCode: String,
        @PriceChartPeriod period: Int
    ): Either<Failure, ChartDataItem>
}
