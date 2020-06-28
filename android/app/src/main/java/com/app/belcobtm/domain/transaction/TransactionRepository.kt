package com.app.belcobtm.domain.transaction

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.transaction.item.SellLimitsDataItem
import com.app.belcobtm.domain.transaction.item.SellPreSubmitDataItem
import com.app.belcobtm.domain.transaction.item.TradeInfoDataItem
import com.app.belcobtm.domain.transaction.item.TransactionDataItem
import com.app.belcobtm.domain.transaction.type.TradeSortType

interface TransactionRepository {
    suspend fun getTransactionList(
        coinCode: String,
        currentListSize: Int
    ): Either<Failure, Pair<Int, List<TransactionDataItem>>>

    suspend fun createTransaction(
        fromCoin: String,
        fromCoinAmount: Double,
        isNeedSendSms: Boolean
    ): Either<Failure, String>

    suspend fun createWithdrawTransaction(
        fromCoin: String,
        fromCoinAmount: Double,
        toAddress: String
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

    suspend fun sellGetLimits(): Either<Failure, SellLimitsDataItem>

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

    suspend fun tradeGetBuyList(
        latitude: Double,
        longitude: Double,
        coinFrom: String,
        sortType: TradeSortType,
        paginationStep: Int
    ): Either<Failure, TradeInfoDataItem>

    suspend fun getTradeSellList(
        latitude: Double,
        longitude: Double,
        coinFrom: String,
        sortType: TradeSortType,
        paginationStep: Int
    ): Either<Failure, TradeInfoDataItem>

    suspend fun getTradeMyList(
        latitude: Double,
        longitude: Double,
        coinFrom: String,
        sortType: TradeSortType,
        paginationStep: Int
    ): Either<Failure, TradeInfoDataItem>

    suspend fun getTradeOpenList(
        latitude: Double,
        longitude: Double,
        coinFrom: String,
        sortType: TradeSortType,
        paginationStep: Int
    ): Either<Failure, TradeInfoDataItem>

    suspend fun tradeBuySell(
        id: Int,
        price: Int,
        fromUsdAmount: Int,
        toCoin: String,
        toCoinAmount: Double,
        detailsText: String
    ): Either<Failure, Unit>

    suspend fun tradeBuyCreate(
        coinCode: String,
        paymentMethod: String,
        margin: Double,
        minLimit: Long,
        maxLimit: Long,
        terms: String
    ): Either<Failure, Unit>

    suspend fun tradeSellCreate(
        coinCode: String,
        paymentMethod: String,
        margin: Double,
        minLimit: Long,
        maxLimit: Long,
        terms: String
    ): Either<Failure, Unit>
}