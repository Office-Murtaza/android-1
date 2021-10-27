package com.belcobtm.data.core.factory

import com.belcobtm.data.disk.database.account.AccountDao
import com.belcobtm.data.rest.transaction.response.hash.TronRawDataResponse
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.transaction.item.TransactionPlanItem
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.presentation.core.extensions.toStringCoin
import com.belcobtm.presentation.core.extensions.unit
import com.belcobtm.presentation.core.toHexByteArray
import com.belcobtm.presentation.core.toHexBytesInByteString
import com.google.protobuf.ByteString
import com.squareup.moshi.Moshi
import wallet.core.jni.CoinType
import wallet.core.jni.PrivateKey
import wallet.core.jni.proto.Tron
import java.lang.RuntimeException
import java.util.*

class TronTransactionInputBuilderFactory(private val accountDao: AccountDao) {

    suspend fun createInput(
        toAddress: String,
        fromCoin: LocalCoinType,
        fromCoinAmount: Double,
        fromTransactionPlan: TransactionPlanItem
    ): Tron.SigningInput.Builder {
        val accountEntity = accountDao.getItem(fromCoin.name)
        val rawData = fromTransactionPlan.blockHeader?.raw_data
        val cryptoToSubcoin = fromCoinAmount.toStringCoin().toDouble() * CoinType.TRON.unit()
        val fromAddress = accountEntity.publicKey
        val parentHash = rawData?.parentHash?.let { "0x$it" }?.toHexByteArray() ?: byteArrayOf()
        val witnessAddress =
            rawData?.witness_address?.let { "0x$it" }?.toHexByteArray() ?: byteArrayOf()
        val txTrieRoot = rawData?.txTrieRoot?.let { "0x$it" }?.toHexByteArray() ?: byteArrayOf()
        val tronBlock = Tron.BlockHeader.newBuilder().also {
            it.number = rawData?.number ?: 0L
            it.timestamp = rawData?.timestamp ?: 0L
            it.version = rawData?.version ?: 0
            it.parentHash = ByteString.copyFrom(parentHash)
            it.witnessAddress = ByteString.copyFrom(witnessAddress)
            it.txTrieRoot = ByteString.copyFrom(txTrieRoot)
        }
        val transferBuilder = Tron.TransferContract.newBuilder().also {
            it.ownerAddress = fromAddress
            it.toAddress = toAddress
            it.amount = cryptoToSubcoin.toLong()
        }
        val transaction = Tron.Transaction.newBuilder().also {
            it.transfer = transferBuilder.build()
            it.timestamp = Date().time
            it.expiration = Calendar.getInstance().also { calendar ->
                calendar.time = Date()
                calendar.add(Calendar.HOUR, 10000)
            }.timeInMillis
            it.blockHeader = tronBlock.build()
        }
        return Tron.SigningInput.newBuilder().also {
            it.transaction = transaction.build()
            it.privateKey = accountEntity.privateKey.toHexBytesInByteString()
        }
    }
}