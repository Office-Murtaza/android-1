package com.belcobtm.data.core.helper

import com.belcobtm.data.core.factory.BlockTransactionInputBuilderFactory
import com.belcobtm.data.rest.transaction.response.hash.UtxoItemResponse
import com.belcobtm.domain.transaction.item.SignedTransactionPlanItem
import com.belcobtm.domain.transaction.item.TransactionPlanItem
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.presentation.core.Numeric
import com.belcobtm.presentation.core.extensions.toStringCoin
import com.belcobtm.presentation.core.extensions.unit
import wallet.core.java.AnySigner
import wallet.core.jni.proto.Bitcoin


class BlockTransactionHelper(private val blockFactory: BlockTransactionInputBuilderFactory) {

    suspend fun getSignedTransactionPlan(
        useMaxAmountFlag: Boolean,
        toAddress: String,
        fromCoin: LocalCoinType,
        fromCoinAmount: Double,
        fromTransactionPlan: TransactionPlanItem,
        utxos: List<UtxoItemResponse>
    ): SignedTransactionPlanItem {
        val input = blockFactory.createInput(
            utxos, toAddress, fromCoin,
            fromCoinAmount, fromTransactionPlan
        )
        input.useMaxAmount = useMaxAmountFlag
        val plan = AnySigner.plan(
            input.build(),
            fromCoin.trustWalletType,
            Bitcoin.TransactionPlan.parser()
        )
        val availableAmount = plan.availableAmount.toDouble() / fromCoin.trustWalletType.unit()
        val fee = plan.fee / fromCoin.trustWalletType.unit().toDouble()
        return SignedTransactionPlanItem(fee, availableAmount)
    }

    suspend fun getHash(
        useMaxAmountFlag: Boolean,
        toAddress: String,
        fromCoin: LocalCoinType,
        fromCoinAmount: Double,
        fromTransactionPlan: TransactionPlanItem,
        utxos: List<UtxoItemResponse>
    ): String {
        val input = blockFactory.createInput(
            utxos, toAddress, fromCoin,
            fromCoinAmount, fromTransactionPlan
        )
        input.useMaxAmount = useMaxAmountFlag
        val plan = AnySigner.plan(
            input.build(),
            fromCoin.trustWalletType,
            Bitcoin.TransactionPlan.parser()
        )
        input.plan = plan
        val signBytes = AnySigner.sign(
            input.build(),
            fromCoin.trustWalletType,
            Bitcoin.SigningOutput.parser()
        ).encoded.toByteArray()
        return Numeric.toHexString(signBytes).substring(2)
    }
}