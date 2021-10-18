package com.belcobtm.data.rest.transaction.response.hash


data class TronBlockHeaderResponse(
    val raw_data: TronRawDataResponse?
)

data class TronRawDataResponse(
    val number: Long?,
    val txTrieRoot: String?,
    val witness_address: String?,
    val parentHash: String?,
    val version: Int?,
    val timestamp: Long?
)