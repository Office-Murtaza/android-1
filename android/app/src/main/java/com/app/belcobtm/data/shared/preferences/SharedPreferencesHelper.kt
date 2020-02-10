package com.app.belcobtm.data.shared.preferences

import android.content.SharedPreferences
import com.app.belcobtm.api.model.response.GetCoinsFeeResponse
import com.google.gson.Gson

class SharedPreferencesHelper(private val sharedPreferences: SharedPreferences) {
    var accessToken: String
        set(value) = sharedPreferences.set(ACCESS_TOKEN, value)
        get() = sharedPreferences[ACCESS_TOKEN] ?: ""

    var refreshToken: String
        set(value) = sharedPreferences.set(REFRESH_TOKEN, value)
        get() = sharedPreferences[REFRESH_TOKEN] ?: ""

    var apiSeed: String?
        set(value) = sharedPreferences.set(API_SEED, value)
        get() = sharedPreferences[API_SEED]

    var userId: Int
        set(value) = sharedPreferences.set(USER_ID, value)
        get() = sharedPreferences[USER_ID] ?: -1

    var userPin: String?
        set(value) = sharedPreferences.set(USER_PIN, value)
        get() = sharedPreferences[USER_PIN]

    var coinsFee: List<GetCoinsFeeResponse.CoinFee>?
        set(value) = sharedPreferences.set(COINS_FEE, Gson().toJson(value))
        get() = try {
            Gson().fromJson(sharedPreferences[COINS_FEE] ?: "", Array<GetCoinsFeeResponse.CoinFee>::class.java).toList()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    companion object {
        private const val ACCESS_TOKEN = "KEY_API_SESSION_TOKEN"
        private const val REFRESH_TOKEN = "KEY_API_REFRESH_TOKEN"
        private const val API_SEED = "KEY_API_SEED"
        private const val USER_ID = "KEY_USER_ID"
        private const val USER_PIN = "KEY_PIN"
        private const val COINS_FEE = "PREF_KEY_COINS_FEE"
    }
}