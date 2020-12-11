package com.app.belcobtm.domain.wallet.item

import android.os.Parcelable
import com.app.belcobtm.domain.wallet.LocalCoinType
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CoinDataItem(
    val balanceCoin: Double,
    val balanceUsd: Double,
    val priceUsd: Double,
    val reservedBalanceCoin: Double,
    val reservedBalanceUsd: Double,
    val code: String,
    val publicKey: String,
    val isEnabled: Boolean = true
) : Parcelable

fun CoinDataItem.isEthRelatedCoin(): Boolean {
    return this.code == LocalCoinType.USDT.name || this.code == LocalCoinType.CATM.name
}