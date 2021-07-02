package com.app.belcobtm.data.core.trx

data class Trx(
    val raw_data: Raw_data,
    val signature: List<String>,
    val txID: String
)