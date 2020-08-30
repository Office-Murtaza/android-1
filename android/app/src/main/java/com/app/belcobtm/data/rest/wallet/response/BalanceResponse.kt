package com.app.belcobtm.data.rest.wallet.response

import com.app.belcobtm.domain.wallet.item.BalanceDataItem
import com.app.belcobtm.domain.wallet.item.CoinDataItem

data class BalanceResponse(
    val coins: List<CoinResponse>,
    val totalBalance: Double
)

data class CoinResponse(
    var balance: Double,
    var fiatBalance: Double,
    var code: String,
    var id: Int,
    var price: Double,
    var address: String,
    var reservedBalance: Double?,
    var reservedFiatBalance: Double?
)

fun BalanceResponse.mapToDataItem(): BalanceDataItem = BalanceDataItem(
    balance = totalBalance,
    coinList = coins.map { it.mapToDataItem() }
)

fun CoinResponse.mapToDataItem(): CoinDataItem = CoinDataItem(
    balanceCoin = balance,
    balanceUsd = fiatBalance,
    priceUsd = price,
    reservedBalanceCoin = reservedBalance ?: 0.0,
    reservedBalanceUsd = reservedFiatBalance ?: 0.0,
    publicKey = address,
    code = code
)
