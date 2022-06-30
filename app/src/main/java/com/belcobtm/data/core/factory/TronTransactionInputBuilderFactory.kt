package com.belcobtm.data.core.factory

import com.belcobtm.R
import com.belcobtm.data.disk.database.account.AccountDao
import com.belcobtm.domain.Failure
import com.belcobtm.domain.transaction.item.TransactionPlanItem
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.presentation.core.provider.string.StringProvider
import com.belcobtm.presentation.core.toHexByteArray
import com.belcobtm.presentation.core.toHexBytesInByteString
import com.belcobtm.presentation.tools.extensions.toStringCoin
import com.belcobtm.presentation.tools.extensions.unit
import com.google.protobuf.ByteString
import wallet.core.jni.CoinType
import wallet.core.jni.proto.Tron
import java.util.Date

class TronTransactionInputBuilderFactory(
    private val accountDao: AccountDao,
    private val stringProvider: StringProvider
) {

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
        if (fromAddress == toAddress) {
            throw Failure.MessageError(stringProvider.getString(R.string.addresses_match_singing_error))
        }
        val parentHash = rawData?.parentHash?.let { "0x$it" }?.toHexByteArray() ?: byteArrayOf()
        val witnessAddress =
            rawData?.witness_address?.let { "0x$it" }?.toHexByteArray() ?: byteArrayOf()
        val txTrieRoot = rawData?.txTrieRoot?.let { "0x$it" }?.toHexByteArray() ?: byteArrayOf()
        val timeStamp = Date().time
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
            it.timestamp = timeStamp
            it.expiration = it.timestamp + 36000000
            it.blockHeader = tronBlock.build()
        }
        return Tron.SigningInput.newBuilder().also {
            it.transaction = transaction.build()
            it.privateKey = accountEntity.privateKey.toHexBytesInByteString()
        }
    }

}
