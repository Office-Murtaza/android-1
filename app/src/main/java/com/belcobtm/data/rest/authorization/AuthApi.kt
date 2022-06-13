package com.belcobtm.data.rest.authorization

import com.belcobtm.data.rest.authorization.request.CheckCredentialsRequest
import com.belcobtm.data.rest.authorization.request.CheckPassRequest
import com.belcobtm.data.rest.authorization.request.CreateWalletRequest
import com.belcobtm.data.rest.authorization.request.RecoverWalletRequest
import com.belcobtm.data.rest.authorization.request.RefreshTokenRequest
import com.belcobtm.data.rest.authorization.response.AuthorizationResponse
import com.belcobtm.data.rest.authorization.response.CheckCredentialsResponse
import com.belcobtm.data.rest.authorization.response.CheckPassResponse
import com.belcobtm.data.rest.authorization.response.CreateRecoverWalletResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

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

    @POST("user/{userId}/password-verify")
    suspend fun checkPass(
        @Path("userId") userId: String,
        @Body checkPassParam: CheckPassRequest
    ): Response<CheckPassResponse>

    @POST("refresh")
    fun refereshToken(
        @Body request: RefreshTokenRequest
    ): Call<AuthorizationResponse>

}
