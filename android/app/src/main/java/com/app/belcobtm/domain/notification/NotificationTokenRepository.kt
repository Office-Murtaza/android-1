package com.app.belcobtm.domain.notification

interface NotificationTokenRepository {

    suspend fun getToken(): String

    fun saveToken(token: String)
}