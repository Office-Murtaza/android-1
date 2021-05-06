package com.app.belcobtm.presentation.core.formatter

import com.app.belcobtm.R
import com.app.belcobtm.data.inmemory.trade.TradeInMemoryCache.Companion.UNDEFINED_DISTANCE
import com.app.belcobtm.presentation.core.provider.string.StringProvider
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class MilesFormatter(private val stringProvider: StringProvider) : Formatter<Double> {

    private val formatter: DecimalFormat

    companion object {
        const val MILES_FORMATTER_QUALIFIER = "MilesFormatter"
    }

    init {
        val formatSymbols = DecimalFormatSymbols(Locale.getDefault())
        formatSymbols.decimalSeparator = '.'
        formatSymbols.groupingSeparator = ','
        val format = "###,##0.00"
        formatter = DecimalFormat(format, formatSymbols)
    }

    override fun format(input: Double): String =
        input.takeIf { it != UNDEFINED_DISTANCE }
            ?.let { stringProvider.getString(R.string.distance_label_formatted, formatter.format(it)) }
            .orEmpty()
}