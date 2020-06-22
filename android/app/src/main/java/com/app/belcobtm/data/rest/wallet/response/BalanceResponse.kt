package com.app.belcobtm.data.rest.wallet.response

import com.app.belcobtm.domain.wallet.item.BalanceDataItem
import com.app.belcobtm.domain.wallet.item.CoinDataItem

data class BalanceResponse(
    val coins: List<CoinResponse>,
    val totalBalance: UsdBalanceResponse
)

data class CoinResponse(
    var balance: Double,
    var code: String,
    var id: Int,
    var price: UsdBalanceResponse,
    var address: String,
    var reservedBalance: Double
)

data class UsdBalanceResponse(val USD: Double)

fun BalanceResponse.mapToDataItem(): BalanceDataItem =
    BalanceDataItem(
        balance = totalBalance.USD,
        coinList = coins.map { it.mapToDataItem() }
    )

fun CoinResponse.mapToDataItem(): CoinDataItem =
    CoinDataItem(
        balanceCoin = balance,
        balanceUsd = balance * price.USD,
        priceUsd = price.USD,
        reservedBalanceCoin = reservedBalance,
        reservedBalanceUsd = reservedBalance * price.USD,
        publicKey = address,
        code = code
    )
