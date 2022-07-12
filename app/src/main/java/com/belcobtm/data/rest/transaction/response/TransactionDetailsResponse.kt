package com.belcobtm.data.rest.transaction.response

import com.belcobtm.domain.transaction.item.TransactionDomainModel
import com.belcobtm.domain.transaction.type.TransactionCashStatusType
import com.belcobtm.domain.transaction.type.TransactionStatusType
import com.belcobtm.domain.transaction.type.TransactionType
import java.util.Calendar

data class TransactionDetailsResponse(
    val id: String?,
    val hash: String?,
    val gbId: String?,
    val link: String?,
    val coin: String?,
    val userId: String?,
    val type: String?,
    val status: String?,
    val cashStatus: String?,
    val cryptoAmount: Double?,
    val fiatAmount: Double?,
    val feePercent: Double?,
    val cryptoFee: Double?,
    val timestamp: Long?,
    val fromPhone: String?,
    val toPhone: String?,
    val fromAddress: String?,
    val toAddress: String?,
    val image: String?,
    val message: String?,
    val sellInfo: String?,
    val refTxId: String?,
    val refLink: String?,
    val refCoin: String?,
    val refCryptoAmount: Double?,
    val confirmations: Int?
) {

    fun mapToDomainModel(coinCode: String): TransactionDomainModel = TransactionDomainModel(
        coinCode = coinCode,
        cryptoAmount = cryptoAmount,
        fiatAmount = fiatAmount,
        cryptoFee = cryptoFee,
        refCryptoAmount = refCryptoAmount,
        hash = hash,
        gbId = gbId.orEmpty(),
        link = link,
        timestamp = timestamp?.takeIf { it > 0 } ?: Calendar.getInstance().timeInMillis, // it's always just sent transaction
        fromPhone = fromPhone,
        toPhone = toPhone,
        fromAddress = fromAddress,
        toAddress = toAddress,
        imageId = image,
        message = message,
        sellInfo = sellInfo,
        refTxId = refTxId,
        refLink = refLink,
        refCoin = refCoin,
        feePercent = feePercent,
        confiramtions = confirmations,
        type = TransactionType.values().firstOrNull { it.name == type } ?: TransactionType.UNKNOWN,
        statusType = TransactionStatusType.values().firstOrNull { it.name == status } ?: TransactionStatusType.UNKNOWN,
        cashStatusType = TransactionCashStatusType.values().firstOrNull { it.name == cashStatus } ?: TransactionCashStatusType.UNKNOWN
    )

}
