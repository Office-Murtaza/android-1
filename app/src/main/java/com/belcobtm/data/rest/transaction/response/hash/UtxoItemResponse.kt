package com.belcobtm.data.rest.transaction.response.hash

data class UtxoItemResponse(
    val address: String?, // ltc1qgaanmhyjhrgk9j9glvtqevmrkrkstjkk5gxspk
    val confirmations: Int?, // 1852
    val height: Long?, // 1701623
    val path: String?, // m/84'/2'/0'/0/0
    val txid: String?, // 31ec4547d652ab9ea2e1a36c92a944896a57836fcbd81b63cc74ed74dcb8c4c1
    val value: String?, // 997192
    val vout: Int? // 1
) {

    fun mapToData() = UtxoItemData(
        address = address.orEmpty(),
        confirmations = confirmations ?: 0,
        height = height ?: 0L,
        path = path.orEmpty(),
        txid = txid.orEmpty(),
        value = value.orEmpty(),
        vout = vout ?: 0
    )

}
