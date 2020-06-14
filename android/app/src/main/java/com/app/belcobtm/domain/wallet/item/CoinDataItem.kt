package com.app.belcobtm.domain.wallet.item

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CoinDataItem(
    val balanceCoin: Double,
    val balanceUsd: Double,
    val priceUsd: Double,
    val reservedBalanceCoin: Double,
    val reservedBalanceUsd: Double,
    val code: String,
    val publicKey: String
) : Parcelable