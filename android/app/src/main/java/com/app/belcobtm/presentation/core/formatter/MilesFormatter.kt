package com.app.belcobtm.presentation.core.formatter

import com.app.belcobtm.R
import com.app.belcobtm.data.inmemory.TradeInMemoryCache.Companion.UNDEFINED_DISTANCE
import com.app.belcobtm.presentation.core.provider.string.StringProvider
import java.text.NumberFormat

class MilesFormatter(private val stringProvider: StringProvider) : Formatter<Double> {

    private val formatter = NumberFormat.getNumberInstance()

    companion object {
        const val MILES_FORMATTER_QUALIFIER = "MilesFormatter"
    }

    init {
        formatter.maximumFractionDigits = 2
    }

    override fun format(input: Double): String =
        input.takeIf { it != UNDEFINED_DISTANCE }
            ?.let { stringProvider.getString(R.string.distance_label_formatted, formatter.format(it)) }
            .orEmpty()
}