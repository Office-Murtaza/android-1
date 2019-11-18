package com.app.belcobtm.api.model.param.trx

import com.app.belcobtm.api.model.param.trx.Contract

data class Raw_data (

	val contract : List<Contract>,
	val expiration : Long,
	val fee_limit : Long,
	val ref_block_bytes : String,
	val ref_block_hash : String,
	val timestamp : Long
)