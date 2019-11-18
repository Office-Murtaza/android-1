package com.app.belcobtm.api.model.param.trx

import com.app.belcobtm.api.model.param.trx.Raw_data

data class Trx (

	val raw_data : Raw_data,
	val signature : List<String>,
	val txID : String
)