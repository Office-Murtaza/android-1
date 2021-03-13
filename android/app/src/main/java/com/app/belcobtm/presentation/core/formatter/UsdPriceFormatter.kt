package com.app.belcobtm.presentation.core.formatter

import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.provider.string.StringProvider
import java.text.NumberFormat
import java.util.*

class UsdPriceFormatter(
    private val stringProvider: StringProvider
) : Formatter<Double> {

//    private val decimalFormat: DecimalFormat

    private val numberFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US)

    companion object {
        const val QUALIFIER = "UsdPriceFormatter"
    }

    init {
        numberFormat.isGroupingUsed = true
        numberFormat.maximumFractionDigits = 2
        //        val formatSymbols = DecimalFormatSymbols(Locale.getDefault())
//        formatSymbols.decimalSeparator = stringProvider.getString(R.string.price_decimal_separator).first()
//        formatSymbols.groupingSeparator = stringProvider.getString(R.string.price_grouping_separator).first()
//        val format = stringProvider.getString(R.string.price_format)
//        decimalFormat = DecimalFormat(format, formatSymbols)
    }

    override fun format(input: Double): String =
        stringProvider.getString(R.string.usd_price_format, numberFormat.format(input))

}