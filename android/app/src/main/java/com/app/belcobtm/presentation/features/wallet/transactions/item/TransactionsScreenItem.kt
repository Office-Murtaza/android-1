package com.app.belcobtm.presentation.features.wallet.transactions.item

class TransactionsScreenItem(
    var balance: Double,
    var priceUsd: Double,
    var reservedBalanceUsd: Double,
    var reservedBalanceCoin: Double,
    var reservedCode: String,
    var chartDay: Pair<Double, List<Double>>,
    var chartWeek: Pair<Double, List<Double>>,
    var chartMonth: Pair<Double, List<Double>>,
    var chartThreeMonths: Pair<Double, List<Double>>,
    var chartYear: Pair<Double, List<Double>>
)