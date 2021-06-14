package com.app.belcobtm.data.rest.wallet.response

import com.app.belcobtm.domain.wallet.item.BalanceDataItem
import com.app.belcobtm.domain.wallet.item.CoinDataItem

data class BalanceResponse(
    val coins: List<CoinResponse>,
    val totalBalance: Double
)

data class CoinResponse(
    val idx: Int,
    val coin: String,
    val address: String,
    val balance: Double,
    val fiatBalance: Double,
    val reserved: Double,
    val fiatReserved: Double,
    val price: Double,
    val details: Details
) {
    data class Details(
        val txFee: Double,
        val byteFee: Long,
        val scale: Int,
        val platformSwapFee: Double,
        val platformTradeFee: Double,
        val walletAddress: String,
        val gasLimit: Long?,
        val gasPrice: Long?,
        val convertedTxFee: Double?
    )
}

fun BalanceResponse.mapToDataItem(): BalanceDataItem = BalanceDataItem(
    balance = totalBalance,
    coinList = coins.sortedBy { it.idx }.map { it.mapToDataItem() }
)

fun CoinResponse.mapToDataItem(): CoinDataItem = CoinDataItem(
    balanceCoin = balance,
    balanceUsd = fiatBalance,
    priceUsd = price,
    reservedBalanceCoin = reserved,
    reservedBalanceUsd = fiatReserved,
    publicKey = address,
    code = coin,
    details = details.mapToDataItem()
)

fun CoinResponse.Details.mapToDataItem() = CoinDataItem.Details(
    txFee = txFee,
    byteFee = byteFee,
    scale = scale,
    platformSwapFee = platformSwapFee,
    platformTradeFee = platformTradeFee,
    walletAddress = walletAddress,
    gasLimit = gasLimit,
    gasPrice = gasPrice,
    convertedTxFee = convertedTxFee
)
