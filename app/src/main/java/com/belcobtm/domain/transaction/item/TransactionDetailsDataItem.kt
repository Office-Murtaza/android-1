package com.belcobtm.domain.transaction.item

import com.belcobtm.domain.transaction.type.TransactionCashStatusType
import com.belcobtm.domain.transaction.type.TransactionStatusType
import com.belcobtm.domain.transaction.type.TransactionType

data class TransactionDetailsDataItem(
    val hash: String?,
    val gbId: String,
    val coinCode: String,
    val cryptoAmount: Double?,
    val fiatAmount: Double?,
    val cryptoFee: Double?,
    val refCryptoAmount: Double?,
    val link: String?,
    val timestamp: Long?,
    val fromPhone: String?,
    val toPhone: String?,
    val fromAddress: String?,
    val toAddress: String?,
    val imageId: String?,
    val message: String?,
    val sellInfo: String?,
    val refTxId: String?,
    val refLink: String?,
    val refCoin: String?,
    val type: TransactionType,
    val statusType: TransactionStatusType,
    val cashStatusType: TransactionCashStatusType,
    val confiramtions: Int?,
    val feePercent: Double?
)