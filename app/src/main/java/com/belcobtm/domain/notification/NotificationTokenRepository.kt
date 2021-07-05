package com.belcobtm.domain.notification

interface NotificationTokenRepository {

    suspend fun getToken(): String

    fun saveToken(token: String)
}