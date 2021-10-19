package com.belcobtm.data.core.helper

import com.belcobtm.data.core.factory.TronTransactionInputBuilderFactory
import com.belcobtm.data.core.trx.Trx
import com.belcobtm.domain.transaction.item.TransactionPlanItem
import com.belcobtm.domain.wallet.LocalCoinType
import com.squareup.moshi.Moshi
import wallet.core.java.AnySigner
import wallet.core.jni.proto.Tron

class TronTransactionHelper(
    private val tronFactory: TronTransactionInputBuilderFactory
) {

    suspend fun getHash(
        toAddress: String,
        fromCoin: LocalCoinType,
        fromCoinAmount: Double,
        fromTransactionPlan: TransactionPlanItem
    ): String {
        val input =
            tronFactory.createInput(toAddress, fromCoin, fromCoinAmount, fromTransactionPlan)
        return AnySigner.sign(
            input.build(),
            fromCoin.trustWalletType,
            Tron.SigningOutput.parser()
        ).json
    }
}