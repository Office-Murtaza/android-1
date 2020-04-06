package com.app.belcobtm.domain.wallet

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure

interface WalletRepository {
    fun getCoinFeeMap(): Map<String, CoinFeeDataItem>
    suspend fun exchangeCoinToCoin(
        coinFromAmount: Double,
        coinFrom: String,
        coinTo: String,
        hex: String
    ): Either<Failure, Unit>
}

