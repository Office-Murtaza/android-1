package com.app.belcobtm.util

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.app.belcobtm.App
import com.app.belcobtm.api.model.response.GetCoinsFeeResponse
import com.google.gson.Gson


class PrefManager private constructor(context: Context) {
    private var mPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    companion object : SingletonHolder<PrefManager, Context>(::PrefManager)

    inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = this.edit()
        operation(editor)
        editor.apply()
    }

    val PREF_KEY_SEED = "KEY_API_SEED"
    val PREF_KEY_API_SESSION_TOKEN = "KEY_API_SESSION_TOKEN"
    val PREF_KEY_API_REFRESH_TOKEN = "KEY_API_REFRESH_TOKEN"
    val PREF_KEY_USER_ID = "KEY_USER_ID"
    val PREF_KEY_PIN = "KEY_PIN"
    val PREF_KEY_COINS_FEE = "PREF_KEY_COINS_FEE"


    fun setSessionApiToken(token: String?) = mPrefs.set(PREF_KEY_API_SESSION_TOKEN, token)
    fun getSessionApiToken(): String? = mPrefs.getString(PREF_KEY_API_SESSION_TOKEN, null)

    fun setRefreshApiToken(refreshToken: String?) =
        mPrefs.set(PREF_KEY_API_REFRESH_TOKEN, refreshToken)

    fun getRefreshApiToken(): String? = mPrefs.getString(PREF_KEY_API_REFRESH_TOKEN, null)

    fun setSeed(seed: String?) = mPrefs.set(PREF_KEY_SEED, seed)
    fun getSeed(): String? = mPrefs.getString(PREF_KEY_SEED, null)

    //    fun getUserId(): Int? = mPrefs[PREF_KEY_USER_ID, -1]
    fun setUserId(userId: Int?) = mPrefs.set(PREF_KEY_USER_ID, userId)

    fun getUserId(): Int = mPrefs.getInt(PREF_KEY_USER_ID, -1)

    fun setPin(pin: String?) = mPrefs.set(PREF_KEY_PIN, pin)
    fun getPin(): String? = mPrefs.getString(PREF_KEY_PIN, null)


    fun saveCoinsFee(resp: GetCoinsFeeResponse?) {

        val prefsEditor: SharedPreferences.Editor = mPrefs.edit()
        val gson = Gson()
        val json = gson.toJson(resp)
        prefsEditor.putString(PREF_KEY_COINS_FEE, json)
        prefsEditor.apply()

    }

    fun getCoinsFee(): List<GetCoinsFeeResponse.CoinFee>? {
        val gsonString = mPrefs.getString(PREF_KEY_COINS_FEE, "")
        val gson = Gson()
        val resp: GetCoinsFeeResponse = try {
            gson.fromJson(gsonString, GetCoinsFeeResponse::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
        return resp.fees
    }


    /**
     * puts a key value pair in shared prefs if doesn't exists, otherwise updates value on given [key]
     */
    operator fun SharedPreferences.set(key: String, value: Any?) {
        when (value) {
            is String? -> edit({ it.putString(key, value) })
            is Int -> edit({ it.putInt(key, value) })
            is Boolean -> edit({ it.putBoolean(key, value) })
            is Float -> edit({ it.putFloat(key, value) })
            is Long -> edit({ it.putLong(key, value) })
            else -> throw UnsupportedOperationException("Not yet implemented")
        }
    }

    /**
     * finds value on given key.
     * [T] is the type of value
     * @param defaultValue optional default value - will take null for strings, false for bool and -1 for numeric values if [defaultValue] is not specified
     */
    operator inline fun <reified T : Any> SharedPreferences.get(
        key: String,
        defaultValue: T? = null
    ): T? {
        return when (T::class) {
            String::class -> getString(key, defaultValue as? String) as T?
            Int::class -> getInt(key, defaultValue as? Int ?: -1) as T?
            Boolean::class -> getBoolean(key, defaultValue as? Boolean ?: false) as T?
            Float::class -> getFloat(key, defaultValue as? Float ?: -1f) as T?
            Long::class -> getLong(key, defaultValue as? Long ?: -1) as T?
            else -> throw UnsupportedOperationException("Not yet implemented")
        }
    }


}

inline val Context.pref: PrefManager
    get() = PrefManager.getInstance(this)

inline val App.pref: PrefManager
    get() = PrefManager.getInstance(this.applicationContext)
