package com.belcobtm.domain.transaction.item

import com.belcobtm.data.rest.transaction.response.hash.TronBlockHeaderResponse
import java.math.BigInteger

data class TransactionPlanItem(
    val coinCode: String,
    val byteFee: Long,
    val nonce: BigInteger,
    val gasPrice: BigInteger,
    val gasLimit: BigInteger,
    val txFee: Double,
    val nativeTxFee: Double,
    val blockHeader: TronBlockHeaderResponse?,
    val accountNumber: Long,
    val sequence: Long,
    val chainId: String
)