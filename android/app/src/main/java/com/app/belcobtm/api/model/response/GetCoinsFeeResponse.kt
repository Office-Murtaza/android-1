package com.app.belcobtm.api.model.response

import com.google.gson.annotations.SerializedName
import org.parceler.Parcel
import java.io.Serializable


data class GetCoinsFeeResponse(
    @SerializedName("fees")
    val fees: List<CoinFee>
) : Serializable {
    data class CoinFee(
        @SerializedName("code")
        val code: String,

        @SerializedName("fee")
        val fee: Double,

        @SerializedName("gasPrice")
        val gasPrice: Double?,

        @SerializedName("gasLimit")
        val gasLimit: Double?
    ) : Serializable
}
