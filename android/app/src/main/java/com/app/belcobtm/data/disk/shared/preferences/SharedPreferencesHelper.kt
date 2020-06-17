package com.app.belcobtm.data.disk.shared.preferences

import android.content.SharedPreferences
import com.app.belcobtm.domain.wallet.item.CoinFeeDataItem
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class SharedPreferencesHelper(private val sharedPreferences: SharedPreferences) {
    private val jsonAdapter: JsonAdapter<Map<String, CoinFeeDataItem>> by lazy {
        val moshi: Moshi = Moshi.Builder().build()
        val listType = Types.newParameterizedType(Map::class.java, String::class.java, CoinFeeDataItem::class.java)
        moshi.adapter<Map<String, CoinFeeDataItem>>(listType)
    }

    var accessToken: String
        set(value) = sharedPreferences.set(
            ACCESS_TOKEN, if (value.isBlank()) "" else ACCESS_TOKEN_BEARER + value
        )
        get() = sharedPreferences[ACCESS_TOKEN] ?: ""

    var refreshToken: String
        set(value) = sharedPreferences.set(REFRESH_TOKEN, value)
        get() = sharedPreferences[REFRESH_TOKEN] ?: ""

    var apiSeed: String
        set(value) = sharedPreferences.set(API_SEED, value)
        get() = sharedPreferences[API_SEED] ?: ""

    var userId: Int
        set(value) = sharedPreferences.set(USER_ID, value)
        get() = sharedPreferences[USER_ID] ?: -1

    var userPin: String
        set(value) = sharedPreferences.set(USER_PIN, value)
        get() = sharedPreferences[USER_PIN] ?: ""

    var coinsFee: Map<String, CoinFeeDataItem>
        set(value) = sharedPreferences.set(COINS_FEE, jsonAdapter.toJson(value))
        get() {
            val json = sharedPreferences[COINS_FEE] ?: ""
            return if (json.isBlank()) emptyMap() else jsonAdapter.fromJson(json) ?: emptyMap()
        }

    var tradeLocationExpirationTime: Long
        set(value) = sharedPreferences.set(TRADE_LOCATION_EXPIRATION_TIME, value)
        get() = sharedPreferences[TRADE_LOCATION_EXPIRATION_TIME] ?: -1

    companion object {
        private const val ACCESS_TOKEN = "KEY_API_SESSION_TOKEN"
        private const val ACCESS_TOKEN_BEARER = "Bearer "

        private const val REFRESH_TOKEN = "KEY_API_REFRESH_TOKEN"
        private const val API_SEED = "KEY_API_SEED"
        private const val USER_ID = "KEY_USER_ID"
        private const val USER_PIN = "KEY_PIN"
        private const val COINS_FEE = "PREF_KEY_COINS_FEE"
        private const val TRADE_LOCATION_EXPIRATION_TIME = "trade_location_expiration_time"
    }
}