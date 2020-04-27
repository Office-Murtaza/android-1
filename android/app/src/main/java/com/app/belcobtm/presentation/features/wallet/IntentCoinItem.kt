package com.app.belcobtm.presentation.features.wallet

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import wallet.core.jni.CoinType

@Parcelize
data class IntentCoinItem(
    val priceUsd: Double,
    val balanceUsd: Double,
    val balanceCoin: Double,
    val coinCode: String,
    val publicKey: String
) : Parcelable