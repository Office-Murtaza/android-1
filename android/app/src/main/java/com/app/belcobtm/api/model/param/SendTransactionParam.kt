package com.app.belcobtm.api.model.param

import com.app.belcobtm.api.model.param.trx.Trx
import com.google.gson.annotations.SerializedName


data class SendTransactionParam(
    @SerializedName("type")
    val type: Int?,

    @SerializedName("cryptoAmount")
    val amount: Double?,

    @SerializedName("phone")
    val phone: String?,

    @SerializedName("message")
    val message: String?,

    @SerializedName("imageId")
    val imageId: String?,

    @SerializedName("hex")
    val hex: String?,

    @SerializedName("trx")
    val trx: Trx?,

    @SerializedName("fromAddress")
    val fromAddress: String? = null,

    @SerializedName("sellFromAnotherAddress")
    val sellFromAnotherAddress: Boolean? = null
)
