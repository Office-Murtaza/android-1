package com.app.belcobtm.api.model.param

import com.app.belcobtm.api.model.param.trx.Trx
import com.google.gson.annotations.SerializedName


data class SendTransactionJsonParam(
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


    val hex: String?,

    @SerializedName("hex")
    val trx: Trx?,

    @SerializedName("fromAddress")
    val fromAddress: String? = null,

    @SerializedName("sellFromAnotherAddress")
    val sellFromAnotherAddress: Boolean? = null
)
