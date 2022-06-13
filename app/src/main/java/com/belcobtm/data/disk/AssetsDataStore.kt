package com.belcobtm.data.disk

import android.content.Context
import com.belcobtm.domain.settings.item.VerificationCountryDataItem
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.Type

class AssetsDataStore(
    moshi: Moshi,
    private val context: Context
    ) {
    private val type: Type = Types.newParameterizedType(List::class.java, VerificationCountryDataItem::class.java)
    private val countryAdapter = moshi.adapter<List<VerificationCountryDataItem>>(type)

    fun getCountries(): List<VerificationCountryDataItem> {
        val fileInString: String = context.assets.open(COUNTRIES_FILENAME).bufferedReader().use { it.readText() }
        return countryAdapter.fromJson(fileInString) ?: emptyList()
    }

    companion object {
        private const val COUNTRIES_FILENAME = "us_cities.json"
    }
}