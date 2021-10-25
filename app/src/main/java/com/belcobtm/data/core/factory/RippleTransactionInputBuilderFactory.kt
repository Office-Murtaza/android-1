package com.belcobtm.data.core.factory

import com.belcobtm.data.disk.database.account.AccountDao
import com.belcobtm.domain.Either
import com.belcobtm.domain.transaction.item.TransactionPlanItem
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.presentation.core.extensions.toStringCoin
import com.belcobtm.presentation.core.extensions.unit
import com.belcobtm.presentation.core.toHexByteArray
import com.google.protobuf.ByteString
import wallet.core.jni.CoinType
import wallet.core.jni.PrivateKey
import wallet.core.jni.proto.Ripple
import java.math.BigDecimal

class RippleTransactionInputBuilderFactory(
    private val accountDao: AccountDao
) {

    suspend fun createInput(
        toAddress: String,
        fromCoin: LocalCoinType,
        fromCoinAmount: Double,
        fromTransactionPlan: TransactionPlanItem
    ): Ripple.SigningInput.Builder {
        val coinEntity = accountDao.getItem(fromCoin.name)
        val privateKey = PrivateKey(coinEntity.privateKey.toHexByteArray())
        return Ripple.SigningInput.newBuilder().also {
            it.sequence = fromTransactionPlan.sequence.toInt()
            it.account = coinEntity.publicKey
            it.amount = (fromCoinAmount.toStringCoin().toDouble() * CoinType.XRP.unit()).toLong()
            it.destination = toAddress
            it.fee = (fromTransactionPlan.txFee * CoinType.XRP.unit()).toLong()
            it.privateKey = ByteString.copyFrom(privateKey.data())
        }
    }
}