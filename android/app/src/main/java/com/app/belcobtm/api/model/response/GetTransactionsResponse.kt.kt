package com.app.belcobtm.api.model.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class GetTransactionsResponse(
    @SerializedName("total")
    val total: Int,
    @SerializedName("transactions")
    val transactions: ArrayList<TransactionModel>
): Serializable

data class TransactionModel(
    @SerializedName("date1")
    val date: String,
    @SerializedName("index")
    val index: Int,
    @SerializedName("status")
    val status: Int,
    @SerializedName("txId")
    val txid: String,
    @SerializedName("type")
    val type: Int,
    @SerializedName("cryptoAmount")
    val value: Double
) : Serializable