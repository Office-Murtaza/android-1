package com.belcobtm.data.core.factory

import com.belcobtm.data.disk.database.account.AccountDao
import com.belcobtm.domain.transaction.item.TransactionPlanItem
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.presentation.core.extensions.code
import com.belcobtm.presentation.core.extensions.toStringCoin
import com.belcobtm.presentation.core.extensions.unit
import com.belcobtm.presentation.core.toHexByteArray
import com.google.protobuf.ByteString
import wallet.core.jni.AnyAddress
import wallet.core.jni.CoinType
import wallet.core.jni.PrivateKey
import wallet.core.jni.proto.Binance

class BinanceTransactionInputBuilderFactory(
    private val accountDao: AccountDao
) {

    suspend fun createInput(
        toAddress: String,
        fromCoin: LocalCoinType,
        fromCoinAmount: Double,
        fromTransactionPlan: TransactionPlanItem
    ): Binance.SigningInput.Builder {
        val coinEntity = accountDao.getItem(fromCoin.name)
        val privateKey = PrivateKey(coinEntity.privateKey.toHexByteArray())
        val fromAddress = privateKey.getPublicKeySecp256k1(true)

        val token = Binance.SendOrder.Token.newBuilder().also {
            it.denom = CoinType.BINANCE.code()
            it.amount = (fromCoinAmount.toStringCoin().toDouble() * CoinType.BINANCE.unit()).toLong()
        }
        val input = Binance.SendOrder.Input.newBuilder().also {
            it.address = ByteString.copyFrom(AnyAddress(fromAddress, CoinType.BINANCE).data())
            it.addAllCoins(listOf(token.build()))
        }
        val output = Binance.SendOrder.Output.newBuilder().also {
            it.address = ByteString.copyFrom(AnyAddress(toAddress, CoinType.BINANCE).data())
            it.addAllCoins(listOf(token.build()))
        }
        val sendOrder = Binance.SendOrder.newBuilder().also {
            it.addAllInputs(listOf(input.build()))
            it.addAllOutputs(listOf(output.build()))
        }
        return Binance.SigningInput.newBuilder().also {
            it.chainId = "Binance-Chain-Tigris"
            it.accountNumber = fromTransactionPlan.accountNumber
            it.sequence = fromTransactionPlan.sequence
            it.privateKey = ByteString.copyFrom(privateKey.data())
            it.sendOrder = sendOrder.build()
        }
    }
}