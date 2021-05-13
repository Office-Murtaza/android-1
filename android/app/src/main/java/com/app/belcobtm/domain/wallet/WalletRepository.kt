package com.app.belcobtm.domain.wallet

import com.app.belcobtm.data.rest.wallet.request.PriceChartPeriod
import com.app.belcobtm.data.websockets.wallet.WalletObserver
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.item.BalanceDataItem
import com.app.belcobtm.domain.wallet.item.ChartDataItem
import com.app.belcobtm.domain.wallet.item.CoinDataItem

interface WalletRepository {

    /**
     * Returns [CoinDataItem] by the specific [coinCode]
     *
     * In case if the [CoinDataItem] is stored under [WalletObserver.observe]
     * return a [CoinDataItem] by the [coinCode]
     *
     * Otherwise if the [CoinDataItem] is not there,
     * the action is delagated to api call
     * */
    suspend fun getCoinItemByCode(coinCode: String): Either<Failure, CoinDataItem>

    /**
     * Returns all the [CoinDataItem]s that are returned by [getBalanceItem]
     * */
    suspend fun getCoinItemList(): Either<Failure, List<CoinDataItem>>

    /**
     * Returns all the [CoinDataItem]s that are stored under [WalletObserver.observe]
     * */
    suspend fun getBalanceItem(): Either<Failure, BalanceDataItem>

    suspend fun getChart(
        coinCode: String,
        @PriceChartPeriod period: Int
    ): Either<Failure, ChartDataItem>
}
