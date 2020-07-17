package com.app.belcobtm.data.rest.authorization

import com.app.belcobtm.data.rest.authorization.request.*
import com.app.belcobtm.data.rest.authorization.response.*
import kotlinx.coroutines.Deferred
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthApi {
    @POST("check")
    fun authorizationCheckCredentialsAsync(
        @Body request: CheckCredentialsRequest
    ): Deferred<Response<CheckCredentialsResponse>>

    @POST("verify")
    fun authorizationVerifySmsCodeAsync(
        @Body request: VerifyPhoneRequest
    ): Deferred<Response<VerifyPhoneResponse>>

    @POST("register")
    fun createWalletAsync(
        @Body request: CreateWalletRequest
    ): Deferred<Response<CreateRecoverWalletResponse>>

    @POST("recover")
    fun recoverWalletAsync(
        @Body request: RecoverWalletRequest
    ): Deferred<Response<CreateRecoverWalletResponse>>

    @POST("user/{userId}/code/verify")
    fun createWalletVerifySmsCodeAsync(
        @Path("userId") userId: Int,
        @Body request: CreateWalletVerifySmsCodeRequest
    ): Deferred<Response<ResponseBody>>

//    @POST("user/{userId}/code/verify")
//    fun authorizationVerifySmsCodeAsync(
//        @Path("userId") userId: Int,
//        @Body request: VerifySmsCodeRequest
//    ): Deferred<Response<ResponseBody>>

    @POST("user/{userId}/coins/add")
    fun addCoinsAsync(
        @Path("userId") userId: Int,
        @Body request: AddCoinsRequest
    ): Deferred<Response<AddCoinsResponse>>

    @POST("refresh")
    fun signInByRefreshTokenAsync(@Body request: RefreshTokenRequest): Deferred<Response<AuthorizationResponse>>
}