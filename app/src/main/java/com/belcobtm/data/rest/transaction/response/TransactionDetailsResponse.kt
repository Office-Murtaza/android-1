package com.belcobtm.data.rest.transaction.response

import com.belcobtm.domain.transaction.item.TransactionDetailsDataItem
import com.belcobtm.domain.transaction.type.TransactionCashStatusType
import com.belcobtm.domain.transaction.type.TransactionStatusType
import com.belcobtm.domain.transaction.type.TransactionType
import java.util.Calendar

data class TransactionDetailsResponse(
    val hash: String?,
    val gbId: String?,
    val link: String?,
    val coin: String?,
    val userId: String?,
    val type: String?,
    val status: TransactionStatusType?,
    val cashStatus: TransactionCashStatusType?,
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

fun TransactionDetailsResponse.mapToDataItem(coinCode: String): TransactionDetailsDataItem = TransactionDetailsDataItem(
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
    statusType = status ?: TransactionStatusType.UNKNOWN,
    cashStatusType = cashStatus ?: TransactionCashStatusType.UNKNOWN
)
