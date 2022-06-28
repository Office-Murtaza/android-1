package com.belcobtm.data.rest.wallet.response

import com.belcobtm.domain.wallet.item.BalanceDataItem
import com.belcobtm.domain.wallet.item.CoinDataItem

data class BalanceResponse(
    val availableCoins: List<String>, // the same format as coin inside CoinResponse
    val coins: List<CoinResponse>,
    val totalBalance: Double
)

data class CoinResponse(
    val coin: String,
    val address: String,
    val price: Double,
    val balance: Double,
    val fiatBalance: Double,
    val reserved: Double,
    val fiatReserved: Double,
    val details: Details
) {

    data class Details(
        val index: Int,
        val serverAddress: String,
        val contractAddress: String?,
    )
}

fun BalanceResponse.mapToDataItem(): BalanceDataItem = BalanceDataItem(
    balance = totalBalance,
    coinList = coins.map { it.mapToDataItem() }
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
    index = index,
    walletAddress = serverAddress,
    contractAddress = contractAddress.orEmpty(),
)
