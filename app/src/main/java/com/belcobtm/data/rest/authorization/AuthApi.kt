package com.belcobtm.data.rest.authorization

import com.belcobtm.data.rest.authorization.request.CheckCredentialsRequest
import com.belcobtm.data.rest.authorization.request.CreateWalletRequest
import com.belcobtm.data.rest.authorization.request.RecoverWalletRequest
import com.belcobtm.data.rest.authorization.request.RefreshTokenRequest
import com.belcobtm.data.rest.authorization.response.AuthorizationResponse
import com.belcobtm.data.rest.authorization.response.CheckCredentialsResponse
import com.belcobtm.data.rest.authorization.response.CreateRecoverWalletResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("check")
    suspend fun authorizationCheckCredentialsAsync(
        @Body request: CheckCredentialsRequest
    ): Response<CheckCredentialsResponse>

    @POST("register")
    suspend fun createWalletAsync(
        @Body request: CreateWalletRequest
    ): Response<CreateRecoverWalletResponse>

    @POST("recover")
    suspend fun recoverWalletAsync(
        @Body request: RecoverWalletRequest
    ): Response<CreateRecoverWalletResponse>

    @POST("refresh")
    suspend fun signInByRefreshTokenAsync(
        @Body request: RefreshTokenRequest
    ): Response<AuthorizationResponse>

    @POST("refresh")
    fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Call<AuthorizationResponse>

}
