package com.app.belcobtm.presentation.features.wallet.transaction.details

import com.app.belcobtm.domain.transaction.item.TransactionDetailsDataItem
import com.app.belcobtm.domain.transaction.type.TransactionCashStatusType
import com.app.belcobtm.domain.transaction.type.TransactionStatusType
import com.app.belcobtm.domain.transaction.type.TransactionType
import com.app.belcobtm.presentation.core.DateFormat

data class TransactionDetailsFragmentItem(
    val txId: String,
    val txDbId: String,
    val cryptoAmount: Double,
    val fiatAmount: Double,
    val cryptoFee: Double,
    val refCryptoAmount: Double,
    val link: String,
    val date: String,
    val fromPhone: String,
    val toPhone: String,
    val fromAddress: String,
    val toAddress: String,
    val imageId: String,
    val message: String,
    val phone: String,
    val sellInfo: String,
    val refTxId: String,
    val refLink: String,
    val refCoin: String,
    val type: TransactionType,
    val statusType: TransactionStatusType,
    val cashStatusType: TransactionCashStatusType
)

fun TransactionDetailsDataItem.mapToUiItem(): TransactionDetailsFragmentItem {
    val formattedDate = when (timestamp) {
        null -> ""
        else -> DateFormat.sdfLong.format(timestamp)
    }
    return TransactionDetailsFragmentItem(
        txId = txId,
        txDbId = txDbId,
        cryptoAmount = cryptoAmount,
        fiatAmount = fiatAmount,
        cryptoFee = cryptoFee,
        refCryptoAmount = refCryptoAmount,
        link = link,
        date = formattedDate,
        fromPhone = fromPhone,
        toPhone = toPhone,
        fromAddress = fromAddress,
        toAddress = toAddress,
        imageId = imageId,
        message = message,
        phone = phone,
        sellInfo = sellInfo,
        refTxId = refTxId,
        refLink = refLink,
        refCoin = refCoin,
        type = type,
        statusType = statusType,
        cashStatusType = cashStatusType
    )
}