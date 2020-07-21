package com.app.belcobtm.data.rest.authorization

import com.app.belcobtm.data.rest.authorization.request.CheckCredentialsRequest
import com.app.belcobtm.data.rest.authorization.request.CreateWalletRequest
import com.app.belcobtm.data.rest.authorization.request.RecoverWalletRequest
import com.app.belcobtm.data.rest.authorization.request.RefreshTokenRequest
import com.app.belcobtm.data.rest.authorization.response.AuthorizationResponse
import com.app.belcobtm.data.rest.authorization.response.CheckCredentialsResponse
import com.app.belcobtm.data.rest.authorization.response.CreateRecoverWalletResponse
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

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
    fun signInByRefreshTokenAsync(@Body request: RefreshTokenRequest): Deferred<Response<AuthorizationResponse>>
}