package com.app.belcobtm.domain.wallet

import com.app.belcobtm.db.DbCryptoCoin
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import wallet.core.jni.CoinType

interface WalletRepository {
    fun getCoinFeeMap(): Map<String, CoinFeeDataItem>

    suspend fun createTransaction(
        fromCoinDb: DbCryptoCoin,//TODO need remove after migration realm to room
        fromCoinCode: String,
        fromCoinAmount: Double,
        isNeedSendSms: Boolean
    ): Either<Failure, String>

    suspend fun withdraw(
        smsCode: String,
        hash: String,
        coinFrom: String,
        coinFromAmount: Double
    ): Either<Failure, Unit>

    suspend fun getGiftAddress(
        coinFrom: String,
        phone: String
    ): Either<Failure, String>

    suspend fun sendGift(
        smsCode: String,
        hash: String,
        coinFrom: String,
        coinFromAmount: Double,
        giftId: String,
        phone: String,
        message: String
    ): Either<Failure, Unit>

    suspend fun exchangeCoinToCoin(
        smsCode: String,
        coinFromAmount: Double,
        coinFrom: String,
        coinTo: String,
        hex: String
    ): Either<Failure, Unit>

    suspend fun sendSmsToDevice(): Either<Failure, Unit>
}

