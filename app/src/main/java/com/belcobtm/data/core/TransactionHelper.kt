package com.belcobtm.data.core

import com.belcobtm.data.core.helper.*
import com.belcobtm.data.rest.transaction.response.hash.UtxoItemResponse
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.transaction.item.SignedTransactionPlanItem
import com.belcobtm.domain.transaction.item.TransactionPlanItem
import com.belcobtm.domain.wallet.LocalCoinType
import wallet.core.jni.EthereumAbiFunction


class TransactionHelper(
    private val blockTransactionHelper: BlockTransactionHelper,
    private val ethTransactionHelper: EthTransactionHelper,
    private val ethSubCoinTransactionHelper: EthSubCoinTransactionHelper,
    private val rippleTransactionHelper: RippleTransactionHelper,
    private val binanceTransactionHelper: BinanceTransactionHelper,
    private val tronTransactionHelper: TronTransactionHelper,
) {

    suspend fun getSignedTransactionPlan(
        toAddress: String,
        fromCoin: LocalCoinType,
        fromCoinAmount: Double,
        fromTransactionPlan: TransactionPlanItem,
        useMaxAmountFlag: Boolean,
        utxos: List<UtxoItemResponse>
    ): Either<Failure, SignedTransactionPlanItem> = when (fromCoin) {
        LocalCoinType.BTC,
        LocalCoinType.BCH,
        LocalCoinType.DOGE,
        LocalCoinType.DASH,
        LocalCoinType.LTC -> Either.Right(
            blockTransactionHelper.getSignedTransactionPlan(
                useMaxAmountFlag, toAddress, fromCoin, fromCoinAmount, fromTransactionPlan, utxos
            )
        )
        LocalCoinType.ETH,
        LocalCoinType.XRP,
        LocalCoinType.BNB,
        LocalCoinType.TRX,
        LocalCoinType.USDC,
        LocalCoinType.CATM -> Either.Right(SignedTransactionPlanItem(fromTransactionPlan.txFee))
    }

    suspend fun createTransactionHash(
        useMaxAmountFlag: Boolean,
        toAddress: String,
        fromCoin: LocalCoinType,
        fromCoinAmount: Double,
        fromTransactionPlan: TransactionPlanItem,
        utxos: List<UtxoItemResponse>
    ): Either<Failure, String> = when (fromCoin) {
        LocalCoinType.BTC,
        LocalCoinType.BCH,
        LocalCoinType.DOGE,
        LocalCoinType.DASH,
        LocalCoinType.LTC -> Either.Right(
            blockTransactionHelper.getHash(
                useMaxAmountFlag, toAddress, fromCoin,
                fromCoinAmount, fromTransactionPlan, utxos
            )
        )
        LocalCoinType.ETH -> Either.Right(
            ethTransactionHelper.getHash(toAddress, fromCoin, fromCoinAmount, fromTransactionPlan)
        )
        LocalCoinType.USDC,
        LocalCoinType.CATM -> Either.Right(
            ethSubCoinTransactionHelper.getHash(
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
    ) = ethSubCoinTransactionHelper.getStakingHash(
        toAddress,
        LocalCoinType.CATM,
        fromCoinAmount,
        fromTransactionPlan,
        EthereumAbiFunction("createStake")
    )

    suspend fun createTransactionStakeCancelHash(
        fromCoinAmount: Double,
        toAddress: String,
        fromTransactionPlan: TransactionPlanItem
    ) = ethSubCoinTransactionHelper.getStakingHash(
        toAddress,
        LocalCoinType.CATM,
        fromCoinAmount,
        fromTransactionPlan,
        EthereumAbiFunction("cancelStake")
    )

    suspend fun createTransactionUnStakeHash(
        fromCoinAmount: Double,
        toAddress: String,
        fromTransactionPlan: TransactionPlanItem
    ) = ethSubCoinTransactionHelper.getStakingHash(
        toAddress,
        LocalCoinType.CATM,
        fromCoinAmount,
        fromTransactionPlan,
        EthereumAbiFunction("withdrawStake")
    )
}