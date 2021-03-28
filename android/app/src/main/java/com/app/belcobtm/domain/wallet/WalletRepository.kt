package com.app.belcobtm.domain.wallet

import com.app.belcobtm.data.rest.wallet.request.PriceChartPeriod
import com.app.belcobtm.data.websockets.wallet.WalletObserver
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.item.*

interface WalletRepository {

    /**
     * Returns [CoinDataItem] by the specific [coinCode]
     *
     * In case if the [CoinDataItem] is stored under [WalletObserver.observe]
     * return a [CoinDataItem] by the [coinCode]
     *
     * Otherwise if the [CoinDataItem] is not there,
     * the action is delagated to [getFreshCoinDataItem]
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

    /**
     * Performs a force api call to retreive [CoinDataItem] by a specific [coinCode]
     * */
    suspend fun getFreshCoinDataItem(coinCode: String): Either<Failure, CoinDataItem>

    /**
     * Performs a force api call to retreive all the [CoinDataItem] that are passed by [coinCodes]
     * */
    suspend fun getFreshCoinDataItems(coinCodes: List<String>): Either<Failure, List<CoinDataItem>>

    suspend fun getChart(
        coinCode: String,
        @PriceChartPeriod period: Int
    ): Either<Failure, ChartDataItem>
}
