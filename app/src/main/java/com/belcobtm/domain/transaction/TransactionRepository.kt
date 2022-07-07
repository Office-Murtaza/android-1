package com.belcobtm.domain.transaction

import android.location.Location
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.transaction.item.SellPreSubmitDataItem
import com.belcobtm.domain.transaction.item.SignedTransactionPlanItem
import com.belcobtm.domain.transaction.item.StakeDetailsDataItem
import com.belcobtm.domain.transaction.item.TransactionDomainModel
import com.belcobtm.domain.transaction.item.TransactionPlanItem
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {

    suspend fun getTransactionPlan(coinCode: String): Either<Failure, TransactionPlanItem>

    suspend fun fetchTransactionList(coinCode: String): Either<Failure, Unit>

    fun observeTransactions(): Flow<Map<String, TransactionDomainModel>>

    suspend fun getSignedPlan(
        fromCoin: String,
        fromCoinAmount: Double,
        fromTransactionPlan: TransactionPlanItem,
        toAddress: String,
        useMaxAmountFlag: Boolean
    ): Either<Failure, SignedTransactionPlanItem>

    suspend fun createTransaction(
        useMaxAmountFlag: Boolean,
        fromCoin: String,
        fromCoinAmount: Double,
        fromTransactionPlan: TransactionPlanItem,
        isNeedSendSms: Boolean
    ): Either<Failure, String>

    suspend fun createTransactionToAddress(
        useMaxAmountFlag: Boolean,
        fromCoin: String,
        fromCoinAmount: Double,
        fromTransactionPlan: TransactionPlanItem,
        toAddress: String,
    ): Either<Failure, String>

    suspend fun receiverAccountActivated(
        fromCoin: String,
        toAddress: String
    ): Either<Failure, Boolean>

    suspend fun withdraw(
        useMaxAmountFlag: Boolean,
        toAddress: String,
        fromCoin: String,
        fromCoinAmount: Double,
        fee: Double,
        fiatAmount: Double,
        fromTransactionPlan: TransactionPlanItem,
        price: Double
    ): Either<Failure, Unit>

    suspend fun sendGift(
        useMaxAmountFlag: Boolean,
        amount: Double,
        coinCode: String,
        giftId: String?,
        phone: String,
        message: String?,
        price: Double,
        fee: Double,
        feePercent: Int?,
        fiatAmount: Double,
        toAddress: String,
        transactionPlanItem: TransactionPlanItem,
        location: Location
    ): Either<Failure, Unit>

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
        useMaxAmountFlag: Boolean,
        fromCoinAmount: Double,
        toCoinAmount: Double,
        fromCoin: String,
        coinTo: String,
        fee: Double,
        fiatAmount: Double,
        transactionPlanItem: TransactionPlanItem,
        location: Location
    ): Either<Failure, Unit>

    suspend fun tradeRecallTransactionComplete(
        coinCode: String,
        cryptoAmount: Double,
        price: Double,
        fiatAmount: Double,
    ): Either<Failure, Unit>

    suspend fun tradeReserveTransactionCreate(
        useMaxAmountFlag: Boolean,
        coinCode: String,
        cryptoAmount: Double,
        transactionPlanItem: TransactionPlanItem
    ): Either<Failure, String>

    suspend fun tradeReserveTransactionComplete(
        coinCode: String,
        cryptoAmount: Double,
        hash: String,
        fee: Double,
        transactionPlanItem: TransactionPlanItem,
        price: Double,
        fiatAmount: Double,
    ): Either<Failure, Unit>

    suspend fun stakeDetails(
        coinCode: String
    ): Either<Failure, StakeDetailsDataItem>

    suspend fun stakeCreate(
        coinCode: String,
        cryptoAmount: Double,
        feePercent: Double,
        fiatAMount: Double,
        transactionPlanItem: TransactionPlanItem,
        location: Location
    ): Either<Failure, Unit>

    suspend fun stakeCancel(
        coinCode: String,
        transactionPlanItem: TransactionPlanItem,
        location: Location
    ): Either<Failure, Unit>

    suspend fun stakeWithdraw(
        coinCode: String,
        cryptoAmount: Double,
        transactionPlanItem: TransactionPlanItem,
        location: Location
    ): Either<Failure, Unit>

    suspend fun getTransferAddress(phone: String, coinCode: String): Either<Failure, String>

}
