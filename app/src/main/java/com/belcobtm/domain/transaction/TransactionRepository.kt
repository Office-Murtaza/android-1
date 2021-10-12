package com.belcobtm.domain.transaction

import com.belcobtm.data.model.transactions.TransactionsData
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.transaction.item.*
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {

    suspend fun getTransactionPlan(coinCode: String): Either<Failure, TransactionPlanItem>

    suspend fun fetchTransactionList(coinCode: String): Either<Failure, Unit>

    fun observeTransactions(): Flow<TransactionsData>

    suspend fun getSignedPlan(
        fromCoin: String,
        fromCoinAmount: Double,
        fromTransactionPlan: TransactionPlanItem,
        toAddress: String,
        useMaxAmountFlag: Boolean
    ): Either<Failure, SignedTransactionPlanItem>

    suspend fun createTransaction(
        fromCoin: String,
        fromCoinAmount: Double,
        fromTransactionPlan: TransactionPlanItem,
        isNeedSendSms: Boolean
    ): Either<Failure, String>

    suspend fun withdraw(
        toAddress: String,
        fromCoin: String,
        fromCoinAmount: Double,
        fee: Double,
        fromTransactionPlan: TransactionPlanItem,
    ): Either<Failure, Unit>

    suspend fun sendGift(
        amount: Double,
        coinCode: String,
        giftId: String?,
        phone: String,
        message: String?,
        fee: Double,
        toAddress: String,
        transactionPlanItem: TransactionPlanItem,
    ): Either<Failure, Unit>

    suspend fun sellGetLimits(): Either<Failure, SellLimitsDataItem>

    suspend fun sellPreSubmit(
        smsCode: String,
        fromCoin: String,
        cryptoAmount: Double,
        toUsdAmount: Int
    ): Either<Failure, SellPreSubmitDataItem>

    suspend fun sell(
        coin: String,
        coinAmount: Double,
        usdAmount: Int,
        fee: Double
    ): Either<Failure, Unit>

    suspend fun exchange(
        fromCoinAmount: Double,
        toCoinAmount: Double,
        fromCoin: String,
        coinTo: String,
        fee: Double,
        transactionPlanItem: TransactionPlanItem,
    ): Either<Failure, Unit>

    suspend fun tradeRecallTransactionComplete(
        coinCode: String,
        cryptoAmount: Double,
    ): Either<Failure, Unit>

    suspend fun tradeReserveTransactionCreate(
        coinCode: String,
        cryptoAmount: Double,
        transactionPlanItem: TransactionPlanItem,
    ): Either<Failure, String>

    suspend fun tradeReserveTransactionComplete(
        coinCode: String,
        cryptoAmount: Double,
        hash: String,
        fee: Double,
        transactionPlanItem: TransactionPlanItem,
    ): Either<Failure, Unit>

    suspend fun stakeDetails(
        coinCode: String
    ): Either<Failure, StakeDetailsDataItem>

    suspend fun stakeCreate(
        coinCode: String,
        cryptoAmount: Double,
        transactionPlanItem: TransactionPlanItem,
    ): Either<Failure, Unit>

    suspend fun stakeCancel(
        coinCode: String,
        transactionPlanItem: TransactionPlanItem,
    ): Either<Failure, Unit>

    suspend fun stakeWithdraw(
        coinCode: String,
        cryptoAmount: Double,
        transactionPlanItem: TransactionPlanItem,
    ): Either<Failure, Unit>

    suspend fun checkXRPAddressActivated(
        address: String
    ): Either<Failure, Boolean>

    suspend fun getTransferAddress(phone: String, coinCode: String): Either<Failure, String>
}
