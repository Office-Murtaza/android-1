package com.app.belcobtm.presentation.core.formatter

import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.provider.string.StringProvider

class TradeCountFormatter(
    private val stringProvider: StringProvider
) : Formatter<Int> {

    companion object {
        const val TRADE_FORMAT_THRESHOLD = 100
        const val TRADE_COUNT_FORMATTER_QUALIFIER = "TradeCountFormatter"
    }

    override fun format(input: Int): String =
        if (input > TRADE_FORMAT_THRESHOLD) {
            stringProvider.getString(R.string.trade_count_over_threshold_formatted, input)
        } else {
            stringProvider.getString(R.string.trade_count_not_over_threshold_formatted, input)
        }
}