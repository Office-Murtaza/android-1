package com.belcobtm.data.core.helper

import com.belcobtm.data.core.factory.BinanceTransactionInputBuilderFactory
import com.belcobtm.domain.transaction.item.TransactionPlanItem
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.presentation.core.Numeric
import wallet.core.java.AnySigner
import wallet.core.jni.proto.Binance

class BinanceTransactionHelper(
    private val binanceFactory: BinanceTransactionInputBuilderFactory
) {

    suspend fun getHash(
        toAddress: String,
        fromCoin: LocalCoinType,
        fromCoinAmount: Double,
        fromTransactionPlan: TransactionPlanItem
    ): String {
        val input =
            binanceFactory.createInput(toAddress, fromCoin, fromCoinAmount, fromTransactionPlan)
        val signBytes = AnySigner.sign(
            input.build(),
            fromCoin.trustWalletType,
            Binance.SigningOutput.parser()
        ).encoded.toByteArray()
        return Numeric.toHexString(signBytes).substring(2)
    }
}