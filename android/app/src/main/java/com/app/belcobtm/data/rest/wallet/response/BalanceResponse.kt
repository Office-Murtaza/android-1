package com.app.belcobtm.data.rest.wallet.response

import com.app.belcobtm.domain.wallet.item.BalanceDataItem
import com.app.belcobtm.domain.wallet.item.CoinDataItem

data class BalanceResponse(
    val coins: List<CoinResponse>,
    val totalBalance: Double
)

data class CoinResponse(
    var balance: Double,
    var code: String,
    var id: Int,
    var price: Double,
    var address: String,
    var reservedBalance: Double?
)

fun BalanceResponse.mapToDataItem(): BalanceDataItem = BalanceDataItem(
    balance = totalBalance,
    coinList = coins.map { it.mapToDataItem() }
)

fun CoinResponse.mapToDataItem(): CoinDataItem = CoinDataItem(
    balanceCoin = balance,
    balanceUsd = balance * price,
    priceUsd = price,
    reservedBalanceCoin = reservedBalance ?: 0.0,
    reservedBalanceUsd = reservedBalance ?: 0.0 * price,
    publicKey = address,
    code = code
)
