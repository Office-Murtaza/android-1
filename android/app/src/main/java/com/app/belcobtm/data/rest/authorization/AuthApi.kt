package com.app.belcobtm.data.rest.authorization

import com.app.belcobtm.data.rest.authorization.request.*
import com.app.belcobtm.data.rest.authorization.response.AuthorizationResponse
import com.app.belcobtm.data.rest.authorization.response.CheckCredentialsResponse
import com.app.belcobtm.data.rest.authorization.response.CheckPassResponse
import com.app.belcobtm.data.rest.authorization.response.CreateRecoverWalletResponse
import kotlinx.coroutines.Deferred
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthApi {
    @POST("check")
    fun authorizationCheckCredentialsAsync(
        @Body request: CheckCredentialsRequest
    ): Deferred<Response<CheckCredentialsResponse>>

    @POST("register")
    fun createWalletAsync(
        @Body request: CreateWalletRequest
    ): Deferred<Response<CreateRecoverWalletResponse>>

    @POST("recover")
    fun recoverWalletAsync(
        @Body request: RecoverWalletRequest
    ): Deferred<Response<CreateRecoverWalletResponse>>

    @POST("refresh")
    fun signInByRefreshTokenAsync(
        @Body request: RefreshTokenRequest): Deferred<Response<AuthorizationResponse>>

    @POST("user/{userId}/password-verify")
    fun checkPass(
        @Path("userId") userId: String,
        @Body checkPassParam: CheckPassRequest
    ): Deferred<Response<CheckPassResponse>>

    @POST("refresh")
    fun refereshToken(@Body request: RefreshTokenRequest): Call<AuthorizationResponse>
}