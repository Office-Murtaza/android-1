package com.belcobtm.data.rest.transaction.response

import com.belcobtm.data.rest.transaction.response.hash.TronBlockHeaderResponse
import com.belcobtm.domain.transaction.item.TransactionPlanItem

//{
//    "byteFee": 1000, //BTC, BCH, LTC, DASH, DOGE
//    "nonce": 15, //ETH, CATM, USDC
//    "gasPrice": 900000000, //ETH, CATM, USDC
//    "gasLimit": 21000, //ETH, CATM, USDC
//    "txFee": 0.001,
//    "nativeTxFee": 5.25, //CATM, USDC
//    "blockHeader": "JSON value", //TRX
//    "accountNumber": 12, //BNB
//    "sequence": 5, //BNB, XRP
//    "chainId": "Binance-Chain-Tigris" //BNB
//}
data class TransactionPlanResponse(
    val byteFee: Long?,
    val nonce: Int?,
    val gasPrice: Int?,
    val gasLimit: Int?,
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
        nonce = nonce ?: 0,
        gasPrice = gasPrice ?: 0,
        gasLimit = gasLimit ?: 0,
        txFee = txFee ?: 0.0,
        nativeTxFee = nativeTxFee ?: 0.0,
        blockHeader = blockHeader,
        accountNumber = accountNumber ?: 0,
        sequence = sequence ?: 0,
        chainId = chainId.orEmpty()
    )