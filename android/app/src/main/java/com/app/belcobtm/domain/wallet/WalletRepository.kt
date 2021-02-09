package com.app.belcobtm.domain.wallet

import com.app.belcobtm.data.rest.wallet.request.PriceChartPeriod
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.item.*

interface WalletRepository {

    fun getCoinDetailsMap(): Map<String, CoinDetailsDataItem>

    fun getCoinDetailsItemByCode(coinCode: String): CoinDetailsDataItem

    suspend fun getCoinItemByCode(coinCode: String): Either<Failure, CoinDataItem>

    suspend fun getCoinItemList(): Either<Failure, List<CoinDataItem>>

    fun updateCoinsCache(coins: List<CoinDataItem>)

    suspend fun getAccountList(): List<AccountDataItem>

    suspend fun updateAccount(accountDataItem: AccountDataItem): Either<Failure, Unit>

    suspend fun getFreshCoinDataItem(coinCode: String): Either<Failure, CoinDataItem>

    suspend fun getFreshCoinDataItems(coinCodes: List<String>): Either<Failure, List<CoinDataItem>>

    suspend fun getBalanceItem(): Either<Failure, BalanceDataItem>

    suspend fun getChart(
        coinCode: String,
        @PriceChartPeriod period: Int
    ): Either<Failure, ChartDataItem>

    suspend fun updateCoinDetails(coinCode: String): Either<Failure, CoinDetailsDataItem>
}

