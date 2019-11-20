package com.app.belcobtm.api.model.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class TransactionDetailsResponse(
    @SerializedName("txId") val txId: String,
    @SerializedName("link") val link: String,
    @SerializedName("type") val type: Int,
    @SerializedName("status") val status: Int,
    @SerializedName("cryptoAmount") val amount: Double,
    @SerializedName("cryptoFee") val fee: Double,
    @SerializedName("date2") val date: String,
    @SerializedName("fromAddress") val fromAddress: String,
    @SerializedName("toAddress") val toAddress: String,
    @SerializedName("imageId") val imageId: String,
    @SerializedName("message") val message: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("sellInfo") val sellInfo: String

) : Serializable
