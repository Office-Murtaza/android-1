package com.belcobtm.data.rest.transaction.response

import com.belcobtm.data.rest.transaction.response.hash.TronBlockHeaderResponse
import com.belcobtm.domain.transaction.item.TransactionPlanItem
import com.squareup.moshi.JsonClass
import java.math.BigInteger

data class TransactionPlanResponse(
    val byteFee: Long?,
    val nonce: String?,
    val gasPrice: String?,
    val gasLimit: String?,
    val txFee: Double?,
    val nativeTxFee: Double?,
    val blockHeader: TronBlockHeaderResponse?,
    val accountNumber: Long?,
    val sequence: Long?,
    val chainId: String?
)

fun TransactionPlanResponse.mapToDataItem(coinCode: String): TransactionPlanItem =
    TransactionPlanItem(
        coinCode = coinCode,
        byteFee = byteFee ?: 0L,
        nonce = nonce?.let(::BigInteger) ?: BigInteger.ZERO,
        gasPrice = gasPrice?.let(::BigInteger) ?: BigInteger.ZERO,
        gasLimit = gasLimit?.let(::BigInteger) ?: BigInteger.ZERO,
        txFee = txFee ?: 0.0,
        nativeTxFee = nativeTxFee ?: 0.0,
        blockHeader = blockHeader,
        accountNumber = accountNumber ?: 0,
        sequence = sequence ?: 0,
        chainId = chainId.orEmpty()
    )