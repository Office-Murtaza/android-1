package com.app.belcobtm.domain.transaction

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.transaction.item.*
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

    suspend fun withdraw(
        fromCoin: String,
        fromCoinAmount: Double,
        toAddress: String
    ): Either<Failure, Unit>

    suspend fun sendGift(
        amount: Double,
        coinCode: String,
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

    suspend fun exchange(
        fromCoinAmount: Double,
        toCoinAmount: Double,
        fromCoin: String,
        coinTo: String
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

    suspend fun tradeRecallTransactionCreate(
        coinCode: String,
        cryptoAmount: Double
    ): Either<Failure, Unit>

    suspend fun tradeRecallTransactionComplete(
        smsCode: String,
        coinCode: String,
        cryptoAmount: Double
    ): Either<Failure, Unit>

    suspend fun tradeReserveTransactionCreate(
        coinCode: String,
        cryptoAmount: Double
    ): Either<Failure, String>

    suspend fun tradeReserveTransactionComplete(
        smsCode: String,
        coinCode: String,
        cryptoAmount: Double,
        hash: String
    ): Either<Failure, Unit>

    suspend fun stakeDetails(
        coinCode: String
    ): Either<Failure, StakeDetailsDataItem>

    suspend fun stakeCreateTransaction(
        coinCode: String,
        cryptoAmount: Double
    ): Either<Failure, String>

    suspend fun stakeCompleteTransaction(
        smsCode: String,
        hash: String,
        coinCode: String,
        cryptoAmount: Double
    ): Either<Failure, Unit>

    suspend fun unStakeCreateTransaction(
        coinCode: String,
        cryptoAmount: Double
    ): Either<Failure, String>

    suspend fun unStakeCompleteTransaction(
        smsCode: String,
        hash: String,
        coinCode: String,
        cryptoAmount: Double
    ): Either<Failure, Unit>

    suspend fun getTransactionDetails(
        txId: String,
        coinCode: String
    ): Either<Failure, TransactionDetailsDataItem>
}