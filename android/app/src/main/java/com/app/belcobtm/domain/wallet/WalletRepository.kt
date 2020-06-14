package com.app.belcobtm.domain.wallet

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.item.BalanceDataItem
import com.app.belcobtm.domain.wallet.item.ChartDataItem
import com.app.belcobtm.domain.wallet.item.CoinFeeDataItem
import com.app.belcobtm.domain.wallet.item.LocalCoinDataItem

interface WalletRepository {

    fun getCoinFeeMap(): Map<String, CoinFeeDataItem>

    suspend fun getLocalCoinList(): List<LocalCoinDataItem>

    suspend fun updateCoin(dataItemLocal: LocalCoinDataItem): Either<Failure, Unit>

    suspend fun getBalanceItem(): Either<Failure, BalanceDataItem>

    suspend fun getChart(coinCode: String): Either<Failure, ChartDataItem>

    suspend fun updateCoinFee(coinCode: String): Either<Failure, CoinFeeDataItem>
}

