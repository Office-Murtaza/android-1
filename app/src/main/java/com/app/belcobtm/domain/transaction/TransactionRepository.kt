package com.app.belcobtm.domain.transaction

import com.app.belcobtm.data.model.transactions.TransactionsData
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.transaction.item.SellLimitsDataItem
import com.app.belcobtm.domain.transaction.item.SellPreSubmitDataItem
import com.app.belcobtm.domain.transaction.item.StakeDetailsDataItem
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {

    suspend fun fetchTransactionList(coinCode: String): Either<Failure, Unit>

    fun observeTransactions(): Flow<TransactionsData>

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
        giftId: String?,
        phone: String,
        message: String?
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

    suspend fun tradeRecallTransactionComplete(
        coinCode: String,
        cryptoAmount: Double
    ): Either<Failure, Unit>

    suspend fun tradeReserveTransactionCreate(
        coinCode: String,
        cryptoAmount: Double
    ): Either<Failure, String>

    suspend fun tradeReserveTransactionComplete(
        coinCode: String,
        cryptoAmount: Double,
        hash: String
    ): Either<Failure, Unit>

    suspend fun stakeDetails(
        coinCode: String
    ): Either<Failure, StakeDetailsDataItem>

    suspend fun stakeCreate(
        coinCode: String,
        cryptoAmount: Double
    ): Either<Failure, Unit>

    suspend fun stakeCancel(
        coinCode: String
    ): Either<Failure, Unit>

    suspend fun stakeWithdraw(
        coinCode: String,
        cryptoAmount: Double
    ): Either<Failure, Unit>

    suspend fun checkXRPAddressActivated(
        address: String
    ): Either<Failure, Boolean>
}
