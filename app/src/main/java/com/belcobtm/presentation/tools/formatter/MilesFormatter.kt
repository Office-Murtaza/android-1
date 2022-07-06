package com.belcobtm.presentation.tools.formatter

import com.belcobtm.R
import com.belcobtm.domain.trade.model.trade.TradeDomainModel.Companion.UNDEFINED_DISTANCE
import com.belcobtm.presentation.core.provider.string.StringProvider
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

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
