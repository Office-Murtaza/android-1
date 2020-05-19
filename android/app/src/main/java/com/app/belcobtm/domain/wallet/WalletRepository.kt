package com.app.belcobtm.domain.wallet

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.item.*

interface WalletRepository {
    fun getCoinFeeMap(): Map<String, CoinFeeDataItem>

    suspend fun getCoinList(): List<CoinDataItem>

    suspend fun updateCoin(dataItem: CoinDataItem): Either<Failure, Unit>

    suspend fun sendSmsToDevice(): Either<Failure, Unit>

    suspend fun verifySmsCode(smsCode: String): Either<Failure, Unit>

    suspend fun createTransaction(
        fromCoin: String,
        fromCoinAmount: Double,
        isNeedSendSms: Boolean
    ): Either<Failure, String>

    suspend fun withdraw(
        smsCode: String,
        hash: String,
        fromCoin: String,
        fromCoinAmount: Double
    ): Either<Failure, Unit>

    suspend fun getGiftAddress(
        fromCoin: String,
        phone: String
    ): Either<Failure, String>

    suspend fun sendGift(
        smsCode: String,
        hash: String,
        fromCoin: String,
        fromCoinAmount: Double,
        giftId: String,
        phone: String,
        message: String
    ): Either<Failure, Unit>

    suspend fun sellGetLimits(
        fromCoin: String
    ): Either<Failure, SellLimitsDataItem>

    suspend fun sellPreSubmit(
        smsCode: String,
        fromCoin: String,
        cryptoAmount: Double,
        toUsdAmount: Int
    ): Either<Failure, SellPreSubmitDataItem>

    suspend fun sell(
        fromCoin: String,
        fromCoinAmount: Double
    ): Either<Failure, Unit>

    suspend fun exchangeCoinToCoin(
        smsCode: String,
        fromCoinAmount: Double,
        fromCoin: String,
        coinTo: String,
        hex: String
    ): Either<Failure, Unit>

    suspend fun getTradeInformation(latitude: Double, longitude: Double): Either<Failure, TradeInfoDataItem>
}

