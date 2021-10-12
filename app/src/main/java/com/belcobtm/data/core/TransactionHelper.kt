package com.belcobtm.data.core

import com.belcobtm.data.core.factory.EthTransactionInputBuilderFactory.Companion.ETH_CATM_FUNCTION_NAME_CANCEL_STAKE
import com.belcobtm.data.core.factory.EthTransactionInputBuilderFactory.Companion.ETH_CATM_FUNCTION_NAME_CREATE_STAKE
import com.belcobtm.data.core.factory.EthTransactionInputBuilderFactory.Companion.ETH_CATM_FUNCTION_NAME_WITHDRAW_STAKE
import com.belcobtm.data.core.helper.*
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.transaction.item.SignedTransactionPlanItem
import com.belcobtm.domain.transaction.item.TransactionPlanItem
import com.belcobtm.domain.wallet.LocalCoinType


class TransactionHelper(
    private val blockTransactionHelper: BlockTransactionHelper,
    private val ethTransactionHelper: EthTransactionHelper,
    private val rippleTransactionHelper: RippleTransactionHelper,
    private val binanceTransactionHelper: BinanceTransactionHelper,
    private val tronTransactionHelper: TronTransactionHelper,
) {

    suspend fun getSignedTransactionPlan(
        toAddress: String,
        fromCoin: LocalCoinType,
        fromCoinAmount: Double,
        fromTransactionPlan: TransactionPlanItem,
        useMaxAmountFlag: Boolean
    ): Either<Failure, SignedTransactionPlanItem> = when (fromCoin) {
        LocalCoinType.BTC,
        LocalCoinType.BCH,
        LocalCoinType.DOGE,
        LocalCoinType.DASH,
        LocalCoinType.LTC -> blockTransactionHelper.getSignedTransactionPlan(
            useMaxAmountFlag, toAddress, fromCoin, fromCoinAmount, fromTransactionPlan
        )
        LocalCoinType.ETH,
        LocalCoinType.XRP,
        LocalCoinType.BNB,
        LocalCoinType.TRX,
        LocalCoinType.USDC,
        LocalCoinType.CATM -> Either.Right(SignedTransactionPlanItem(fromTransactionPlan.txFee))
    }

    suspend fun createTransactionHash(
        toAddress: String,
        fromCoin: LocalCoinType,
        fromCoinAmount: Double,
        fromTransactionPlan: TransactionPlanItem
    ): Either<Failure, String> = when (fromCoin) {
        LocalCoinType.BTC,
        LocalCoinType.BCH,
        LocalCoinType.DOGE,
        LocalCoinType.DASH,
        LocalCoinType.LTC -> blockTransactionHelper.getHash(
            toAddress, fromCoin, fromCoinAmount, fromTransactionPlan
        )
        LocalCoinType.ETH,
        LocalCoinType.USDC,
        LocalCoinType.CATM -> Either.Right(
            ethTransactionHelper.getHash(
                toAddress, fromCoin, fromCoinAmount, fromTransactionPlan
            )
        )
        LocalCoinType.XRP -> Either.Right(
            rippleTransactionHelper.getHash(
                toAddress, fromCoin, fromCoinAmount, fromTransactionPlan
            )
        )
        LocalCoinType.BNB -> Either.Right(
            binanceTransactionHelper.getHash(
                toAddress, fromCoin, fromCoinAmount, fromTransactionPlan
            )
        )
        LocalCoinType.TRX -> Either.Right(
            tronTransactionHelper.getHash(
                toAddress, fromCoin, fromCoinAmount, fromTransactionPlan
            )
        )
    }

    suspend fun createTransactionStakeHash(
        fromCoinAmount: Double,
        toAddress: String,
        fromTransactionPlan: TransactionPlanItem
    ) = ethTransactionHelper.getHash(
        toAddress,
        LocalCoinType.CATM,
        fromCoinAmount,
        fromTransactionPlan,
        ETH_CATM_FUNCTION_NAME_CREATE_STAKE
    )

    suspend fun createTransactionStakeCancelHash(
        fromCoinAmount: Double,
        toAddress: String,
        fromTransactionPlan: TransactionPlanItem
    ) = ethTransactionHelper.getHash(
        toAddress,
        LocalCoinType.CATM,
        fromCoinAmount,
        fromTransactionPlan,
        ETH_CATM_FUNCTION_NAME_CANCEL_STAKE
    )

    suspend fun createTransactionUnStakeHash(
        fromCoinAmount: Double,
        toAddress: String,
        fromTransactionPlan: TransactionPlanItem
    ) = ethTransactionHelper.getHash(
        toAddress,
        LocalCoinType.CATM,
        fromCoinAmount,
        fromTransactionPlan,
        ETH_CATM_FUNCTION_NAME_WITHDRAW_STAKE
    )
}