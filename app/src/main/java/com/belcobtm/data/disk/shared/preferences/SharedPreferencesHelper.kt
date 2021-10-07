package com.belcobtm.data.disk.shared.preferences

import android.content.SharedPreferences
import com.belcobtm.data.rest.authorization.response.AuthorizationResponse

class SharedPreferencesHelper(private val sharedPreferences: SharedPreferences) {

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

    var firebaseToken: String
        set(value) = sharedPreferences.set(FIREBASE_TOKEN, value)
        get() = sharedPreferences[FIREBASE_TOKEN] ?: ""

    var userId: String
        set(value) = sharedPreferences.set(USER_ID, value)
        get() = sharedPreferences[USER_ID] ?: ""

    var userPin: String
        set(value) = sharedPreferences.set(USER_PIN, value)
        get() = sharedPreferences[USER_PIN] ?: ""

    var notificationToken: String?
        set(value) = sharedPreferences.set(NOTIFICATION_TOKEN, value)
        get() = sharedPreferences[NOTIFICATION_TOKEN]

    var userPhone: String
        set(value) = sharedPreferences.set(USER_PHONE, value)
        get() = sharedPreferences[USER_PHONE] ?: ""

    var userAllowedBioAuth: Boolean
        set(value) = sharedPreferences.set(USER_BIO_AUTH, value)
        get() = sharedPreferences.getBoolean(USER_BIO_AUTH, true)

    var referralCode: String
        set(value) = sharedPreferences.set(REFERRAL_CODE, value)
        get() = sharedPreferences[REFERRAL_CODE] ?: ""

    var referralInvites: Int
        set(value) = sharedPreferences.set(REFERRAL_INVITES, value)
        get() = sharedPreferences[REFERRAL_INVITES] ?: 0

    var referralEarned: Int
        set(value) = sharedPreferences.set(REFERRAL_EARNED, value)
        get() = sharedPreferences[REFERRAL_EARNED] ?: 0

    fun processAuthResponse(authorizationResponse: AuthorizationResponse) {
        authorizationResponse.let {
            accessToken = it.accessToken
            refreshToken = it.refreshToken
            firebaseToken = it.firebaseToken
            userId = it.user.id
            referralCode = it.user.referralCode.orEmpty()
            referralInvites = it.user.referrals ?: 0
            referralEarned = it.user.referralEarned ?: 0
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
        val token = notificationToken
        sharedPreferences.edit()
            .clear()
            .apply()
        notificationToken = token
    }

    companion object {
        const val ACCESS_TOKEN = "KEY_API_SESSION_TOKEN"
        private const val ACCESS_TOKEN_BEARER = "Bearer "

        private const val REFRESH_TOKEN = "KEY_API_REFRESH_TOKEN"
        private const val API_SEED = "KEY_API_SEED"
        private const val FIREBASE_TOKEN = "FIREBASE_TOKEN"
        private const val USER_ID = "KEY_USER_ID"
        private const val USER_PIN = "KEY_PIN"
        private const val NOTIFICATION_TOKEN = "KEY_NOTIFICATION"
        private const val USER_PHONE = "KEY_PHONE"
        private const val USER_BIO_AUTH = "KEY_BIO_AUTH"
        private const val REFERRAL_CODE = "REFERRAL_CODE"
        private const val REFERRAL_INVITES = "REFERRAL_INVITES"
        private const val REFERRAL_EARNED = "REFERRAL_EARNED"
    }
}