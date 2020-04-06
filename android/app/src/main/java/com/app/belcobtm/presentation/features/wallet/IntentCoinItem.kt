package com.app.belcobtm.presentation.features.wallet

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class IntentCoinItem(
    val priceUsd: Double,
    val balanceUsd: Double,
    val balanceCoin: Double,
    val coinCode: String
) : Parcelable