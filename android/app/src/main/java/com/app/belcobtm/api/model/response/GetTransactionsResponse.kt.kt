package com.app.belcobtm.api.model.response

import com.google.gson.annotations.SerializedName


data class GetTransactionsResponse(
    @SerializedName("total")
    val total: Int, // 12
    @SerializedName("transactions")
    val transactions: ArrayList<TransactionModel>
)

data class TransactionModel(
    @SerializedName("date")
    val date: String, // 2019-08-17
    @SerializedName("index")
    val index: Int, // 10
    @SerializedName("status")
    val status: Int, // 2
    @SerializedName("txid")
    val txid: String, // b53d6f6614218a6d7a6b23cd89150908e8112d8717dc2ba2c7bf2997a8c16e09
    @SerializedName("type")
    val type: Int, // 5
    @SerializedName("value")
    val value: Double // 0.01
)