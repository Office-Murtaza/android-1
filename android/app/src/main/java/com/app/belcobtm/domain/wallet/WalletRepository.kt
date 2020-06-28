package com.app.belcobtm.domain.wallet

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.item.*

interface WalletRepository {

    fun getCoinFeeMap(): Map<String, CoinFeeDataItem>

    fun getCoinFeeItemByCode(coinCode: String): CoinFeeDataItem

    fun getCoinItemByCode(coinCode: String): CoinDataItem

    suspend fun getAccountList(): List<AccountDataItem>

    suspend fun updateAccount(accountDataItem: AccountDataItem): Either<Failure, Unit>

    suspend fun getBalanceItem(): Either<Failure, BalanceDataItem>

    suspend fun getChart(coinCode: String): Either<Failure, ChartDataItem>

    suspend fun updateCoinFee(coinCode: String): Either<Failure, CoinFeeDataItem>
}

