package com.belcobtm.domain.transaction.item

import com.belcobtm.data.rest.transaction.response.hash.TronBlockHeaderResponse

data class TransactionPlanItem(
    val coinCode: String,
    val byteFee: Long,
    val nonce: Int,
    val gasPrice: Int,
    val gasLimit: Int,
    val txFee: Double,
    val nativeTxFee: Double,
    val blockHeader: TronBlockHeaderResponse?,
    val accountNumber: Long,
    val sequence: Long,
    val chainId: String
)