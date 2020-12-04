package com.app.belcobtm.data.disk.shared.preferences

import android.content.SharedPreferences
import com.app.belcobtm.data.rest.authorization.response.AuthorizationResponse
import com.app.belcobtm.domain.wallet.item.CoinDetailsDataItem
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class SharedPreferencesHelper(private val sharedPreferences: SharedPreferences) {
    private val jsonAdapter: JsonAdapter<Map<String, CoinDetailsDataItem>> by lazy {
        val moshi: Moshi = Moshi.Builder().build()
        val listType = Types.newParameterizedType(
            Map::class.java,
            String::class.java,
            CoinDetailsDataItem::class.java
        )
        moshi.adapter<Map<String, CoinDetailsDataItem>>(listType)
    }

    var accessToken: String = ""
        set(value) {
            field = if (value.isBlank()) "" else formatToken(value)
        }

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

    var notificationToken: String?
        set(value) = sharedPreferences.set(NOTIFICATION_TOKEN, value)
        get() = sharedPreferences[NOTIFICATION_TOKEN]

    var coinsDetails: Map<String, CoinDetailsDataItem>
        set(value) = sharedPreferences.set(COINS_FEE, jsonAdapter.toJson(value))
        get() {
            val json = sharedPreferences[COINS_FEE] ?: ""
            return if (json.isBlank()) emptyMap() else jsonAdapter.fromJson(json) ?: emptyMap()
        }
    var userPhone: String
        set(value) = sharedPreferences.set(USER_PHONE, value)
        get() = sharedPreferences[USER_PHONE] ?: ""

    var tradeLocationExpirationTime: Long
        set(value) = sharedPreferences.set(TRADE_LOCATION_EXPIRATION_TIME, value)
        get() = sharedPreferences[TRADE_LOCATION_EXPIRATION_TIME] ?: -1

    fun processAuthResponse(authorizationResponse: AuthorizationResponse) {
        authorizationResponse.let {
            accessToken = it.accessToken
            refreshToken = it.refreshToken
            userId = it.userId
        }
    }

    /**
     * Formats given token to the one with [ACCESS_TOKEN_BEARER] prefix
     *
     * @param token raw token to be formatted
     * @return formatted token with [ACCESS_TOKEN_BEARER] prefix
     * */
    fun formatToken(token: String): String {
        return ACCESS_TOKEN_BEARER.plus(token)
    }

    fun clearValue(key: String) {
        sharedPreferences[key] = null
    }

    fun clearData() {
        accessToken = ""
        refreshToken = ""
        apiSeed = ""
        userPin = ""
        userId = -1
    }

    companion object {
        const val ACCESS_TOKEN = "KEY_API_SESSION_TOKEN"
        private const val ACCESS_TOKEN_BEARER = "Bearer "

        private const val REFRESH_TOKEN = "KEY_API_REFRESH_TOKEN"
        private const val API_SEED = "KEY_API_SEED"
        private const val USER_ID = "KEY_USER_ID"
        private const val USER_PIN = "KEY_PIN"
        private const val NOTIFICATION_TOKEN = "KEY_NOTIFICATION"
        private const val USER_PHONE = "KEY_PHONE"
        private const val COINS_FEE = "PREF_KEY_COINS_FEE"
        private const val TRADE_LOCATION_EXPIRATION_TIME = "trade_location_expiration_time"
    }
}