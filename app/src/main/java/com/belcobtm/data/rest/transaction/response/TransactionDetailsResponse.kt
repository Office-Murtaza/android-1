package com.belcobtm.data.rest.transaction.response

import com.belcobtm.domain.transaction.item.TransactionDetailsDataItem
import com.belcobtm.domain.transaction.type.TransactionCashStatusType
import com.belcobtm.domain.transaction.type.TransactionStatusType
import com.belcobtm.domain.transaction.type.TransactionType

data class TransactionDetailsResponse(
    val txId: String?,
    val txDBId: String?,
    val link: String?,
    val coin: String?,
    val userId: String,
    val type: Int?,
    val status: Int,
    val cashStatus: Int,
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
)

fun TransactionDetailsResponse.mapToDataItem(coinCode: String): TransactionDetailsDataItem =
    TransactionDetailsDataItem(
        coinCode = coinCode,
        cryptoAmount = cryptoAmount,
        fiatAmount = fiatAmount,
        cryptoFee = cryptoFee,
        refCryptoAmount = refCryptoAmount,
        txId = txId,
        txDbId = txDBId.orEmpty(),
        link = link,
        timestamp = timestamp,
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
        type = TransactionType.values().firstOrNull { it.code == type } ?: TransactionType.UNKNOWN,
        statusType = TransactionStatusType.values().firstOrNull { it.code == status }
            ?: TransactionStatusType.UNKNOWN,
        cashStatusType = TransactionCashStatusType.values().firstOrNull { it.code == cashStatus }
            ?: TransactionCashStatusType.UNKNOWN
    )
