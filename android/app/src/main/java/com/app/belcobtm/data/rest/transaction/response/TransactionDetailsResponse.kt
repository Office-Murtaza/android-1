package com.app.belcobtm.data.rest.transaction.response

import com.app.belcobtm.domain.transaction.item.TransactionDetailsDataItem
import com.app.belcobtm.domain.transaction.type.TransactionCashStatusType
import com.app.belcobtm.domain.transaction.type.TransactionStatusType
import com.app.belcobtm.domain.transaction.type.TransactionType

data class TransactionDetailsResponse(
    val txId: String?,
    val txDbId: String?,
    val link: String?,
    val type: Int?,
    val status: Int,
    val cashStatus: Int,
    val cryptoAmount: Double?,
    val fiatAmount: Double?,
    val cryptoFee: Double?,
    val timestamp: Long?,
    val fromPhone: String?,
    val toPhone: String?,
    val fromAddress: String?,
    val toAddress: String?,
    val imageId: String?,
    val message: String?,
    val phone: String?,
    val sellInfo: String?,
    val refTxId: String?,
    val refLink: String?,
    val refCoin: String?,
    val refCryptoAmount: Double?,
    val confirmations: Int?,
    val swapCoin: String?,
    val swapCryptoAmount: Double?,
    val swapTxId: String?,
    val swapLink: String?
)

fun TransactionDetailsResponse.mapToDataItem(): TransactionDetailsDataItem =
    TransactionDetailsDataItem(
        cryptoAmount = cryptoAmount,
        fiatAmount = fiatAmount,
        cryptoFee = cryptoFee,
        refCryptoAmount = refCryptoAmount ?: -1.0,
        txId = txId,
        txDbId = txDbId ?: "",
        link = link,
        swapLink = swapLink,
        timestamp = timestamp,
        fromPhone = fromPhone,
        toPhone = toPhone,
        fromAddress = fromAddress,
        toAddress = toAddress,
        imageId = imageId,
        message = message,
        phone = phone ?: "",
        sellInfo = sellInfo,
        refTxId = refTxId ?: "",
        refLink = refLink ?: "",
        refCoin = refCoin ?: "",
        swapCoin = swapCoin,
        swapId = swapTxId,
        swapCryptoAmount = swapCryptoAmount,
        confiramtions = confirmations,
        type = TransactionType.values().firstOrNull { it.code == type } ?: TransactionType.UNKNOWN,
        statusType = TransactionStatusType.values().firstOrNull { it.code == status }
            ?: TransactionStatusType.UNKNOWN,
        cashStatusType = TransactionCashStatusType.values().firstOrNull { it.code == cashStatus }
            ?: TransactionCashStatusType.UNKNOWN
    )
