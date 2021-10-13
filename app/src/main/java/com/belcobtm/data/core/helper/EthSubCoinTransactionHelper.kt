package com.belcobtm.data.core.helper

import com.belcobtm.data.core.factory.EthTransactionInputBuilderFactory
import com.belcobtm.domain.transaction.item.TransactionPlanItem
import com.belcobtm.domain.wallet.LocalCoinType

// Applicable only for CATM and USDC
class EthSubCoinTransactionHelper(
    private val ethFactory: EthTransactionInputBuilderFactory
) {

    suspend fun getHash(
        toAddress: String,
        fromCoin: LocalCoinType,
        fromCoinAmount: Double,
        fromTransactionPlan: TransactionPlanItem
    ): String = ethFactory.createForSubEth(
        toAddress,
        fromCoin,
        fromCoinAmount,
        fromTransactionPlan
    )
}