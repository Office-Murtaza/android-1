package com.app.belcobtm.domain.wallet

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import wallet.core.jni.CoinType

interface WalletRepository {
    fun getCoinFeeMap(): Map<String, CoinFeeDataItem>


    suspend fun createTransaction(fromCoinCode: String, fromCoinAmount: Double): Either<Failure, String>

    suspend fun withdraw(
        smsCode: String,
        hash: String,
        coinFrom: String,
        coinFromAmount: Double
    ): Either<Failure, Unit>

    suspend fun exchangeCoinToCoin(
        smsCode: String,
        coinFromAmount: Double,
        coinFrom: String,
        coinTo: String,
        hex: String
    ): Either<Failure, Unit>
}

