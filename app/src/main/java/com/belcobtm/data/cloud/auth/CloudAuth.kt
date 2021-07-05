package com.belcobtm.data.cloud.auth

interface CloudAuth {
    suspend fun currentUserExists(): Boolean

    suspend fun authWithToken(token: String)
}