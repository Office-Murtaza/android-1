package com.belcobtm.data.core.helper

import com.belcobtm.data.core.factory.EthTransactionInputBuilderFactory
import com.belcobtm.domain.transaction.item.TransactionPlanItem
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.presentation.core.Numeric
import wallet.core.java.AnySigner
import wallet.core.jni.proto.Ethereum

class EthTransactionHelper(
    private val ethFactory: EthTransactionInputBuilderFactory
) {

    suspend fun getHash(
        toAddress: String,
        fromCoin: LocalCoinType,
        fromCoinAmount: Double,
        fromTransactionPlan: TransactionPlanItem,
        customFunctionName: String? = null
    ): String {
        val input = ethFactory.createInput(
            toAddress,
            fromCoin,
            fromCoinAmount,
            fromTransactionPlan,
            customFunctionName
        )
        val signBytes = AnySigner.sign(
            input.build(),
            fromCoin.trustWalletType,
            Ethereum.SigningOutput.parser()
        ).encoded.toByteArray()
        return Numeric.toHexString(signBytes)
    }
}