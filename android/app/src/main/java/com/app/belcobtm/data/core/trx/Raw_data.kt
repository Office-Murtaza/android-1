package com.app.belcobtm.data.core.trx

data class Raw_data(

    val contract: List<Contract>,
    val expiration: Long,
    val fee_limit: Long,
    val ref_block_bytes: String,
    val ref_block_hash: String,
    val timestamp: Long
)