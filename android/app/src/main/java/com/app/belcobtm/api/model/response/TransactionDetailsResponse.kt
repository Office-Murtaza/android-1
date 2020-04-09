package com.app.belcobtm.api.model.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class TransactionDetailsResponse(
    @SerializedName("txId") val txId: String?,
    @SerializedName("txDbId") val txDbId: Int?,
    @SerializedName("link") val link: String,
    @SerializedName("type") val type: Int?,
    @SerializedName("status") val status: Int,
    @SerializedName("cashStatus") val cashStatus: Int,
    @SerializedName("cryptoAmount") val amount: Double?,
    @SerializedName("fiatAmount") val fiatAmount: Double?,
    @SerializedName("cryptoFee") val fee: Double?,
    @SerializedName("date2") val date: String?,
    @SerializedName("fromAddress") val fromAddress: String?,
    @SerializedName("toAddress") val toAddress: String?,
    @SerializedName("imageId") val imageId: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("sellInfo") val sellInfo: String?,
    @SerializedName("refTxId") val refTxId: String?,
    @SerializedName("refLink") val refLink: String?,
    @SerializedName("refCoin") val refCoin: String?,
    @SerializedName("refCryptoAmount") val refCryptoAmount: Double?
) : Serializable {

    fun getStatusType(): TransactionStatusType = TransactionStatusType.values()[status]

    fun getCashStatusType(): TransactionCashStatusType = TransactionCashStatusType.values()[cashStatus]
}

enum class TransactionStatusType {
    UNKNOWN,
    PENDING,
    COMPLETE,
    FAIL
}

enum class TransactionCashStatusType {
    UNKNOWN,
    NOT_AVAILABLE,
    AVAILABLE,
    WITHDRAWN
}