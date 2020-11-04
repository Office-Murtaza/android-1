package com.app.belcobtm.domain.wallet

import com.app.belcobtm.data.websockets.wallet.WalletConnectionHandler
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.item.*
import kotlinx.coroutines.flow.Flow

interface WalletRepository : WalletConnectionHandler {

    fun observerWalletBalance(): Flow<BalanceDataItem>

    fun getCoinDetailsMap(): Map<String, CoinDetailsDataItem>

    fun getCoinDetailsItemByCode(coinCode: String): CoinDetailsDataItem

    fun getCoinItemByCode(coinCode: String): CoinDataItem

    fun getCoinItemList(): List<CoinDataItem>

    suspend fun getAccountList(): List<AccountDataItem>

    suspend fun updateAccount(accountDataItem: AccountDataItem): Either<Failure, Unit>

    suspend fun getFreshCoinDataItem(coinCode: String): Either<Failure, CoinDataItem>

    suspend fun getBalanceItem(): Either<Failure, BalanceDataItem>

    suspend fun getChart(coinCode: String): Either<Failure, ChartDataItem>

    suspend fun updateCoinDetails(coinCode: String): Either<Failure, CoinDetailsDataItem>
}

