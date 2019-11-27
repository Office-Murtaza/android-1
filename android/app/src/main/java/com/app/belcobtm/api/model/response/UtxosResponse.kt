package com.app.belcobtm.api.model.response
import com.google.gson.annotations.SerializedName


data class UtxosResponse(
    @SerializedName("utxos")
    val utxoList: ArrayList<UtxoItem>
)