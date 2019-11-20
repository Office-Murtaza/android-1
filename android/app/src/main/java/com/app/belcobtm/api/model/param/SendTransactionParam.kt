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

/*


"type": 3, //transaction type
"amount": 0.0024,
"phone": "+12018885558", //only for send gift
"message": "Happy Birth Day!!!", //only for send gift
"image": "https://media.giphy.com/media/uGGT9wVlxPAuk/giphy.gif", //only for send gift
"fromAddress": "1bnepreproefjdskfjsgfsdhfsdhhfhsdf" //only for sell from another address
"hex": "c001f0625dee0a4a2a2c87fa0a210a14281b8514c6928a45b0084aa42c01310afa6f668c12090a03424e4210c0843d12210a1455042d6fe839cf6eab4acff7fa338f2c6f9ef7b012090a03424e4210c0843d126e0a26eb5ae9872102f994bd4911fccced511c279d52ed8c298c9ef2d9a605208296408b5a9e28f8b71240f6386d2a3082afa877bdd50cffb5e86cfe5914dbcae9b852512909b449586b5d534a867afd09b101083fb6158065222987ea1d70b4af64bdf296fab3542fc44a18ac9211", //for BTC, BCH, LTC, ETH, BNB, XRP
"trx": {} //only TRX transaction body
*/
