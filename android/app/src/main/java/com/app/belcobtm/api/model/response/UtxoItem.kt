package com.app.belcobtm.api.model.response
import com.google.gson.annotations.SerializedName


data class UtxoItem(
    @SerializedName("address")
    val address: String, // ltc1qgaanmhyjhrgk9j9glvtqevmrkrkstjkk5gxspk
    @SerializedName("confirmations")
    val confirmations: Int, // 1852
    @SerializedName("height")
    val height: Int, // 1701623
    @SerializedName("path")
    val path: String, // m/84'/2'/0'/0/0
    @SerializedName("txid")
    val txid: String, // 31ec4547d652ab9ea2e1a36c92a944896a57836fcbd81b63cc74ed74dcb8c4c1
    @SerializedName("value")
    val value: String, // 997192
    @SerializedName("vout")
    val vout: Int // 1
)