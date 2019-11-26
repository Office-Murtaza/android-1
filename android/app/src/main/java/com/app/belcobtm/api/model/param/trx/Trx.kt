package com.app.belcobtm.api.model.param.trx

data class Trx(

    val raw_data: Raw_data,
    val signature: List<String>,
    val txID: String
)