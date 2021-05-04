package com.app.belcobtm.data.rest.transaction.response

data class GetTransactionsResponse(
    val total: Int,
    val transactions: List<TransactionDetailsResponse>
)