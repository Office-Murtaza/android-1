package com.belcobtm.data.rest.transaction.request

data class WithdrawRequest(
    val type: String = TYPE,
    val hex: String,
    val fromAddress: String?,
    val toAddress: String?,
    val cryptoAmount: Double,
    val price: Double,
    val fee: Double?,
    val latitude: Double?,
    val longitude: Double?,
) {

    companion object {

        private const val TYPE = "WITHDRAW"
    }

}
