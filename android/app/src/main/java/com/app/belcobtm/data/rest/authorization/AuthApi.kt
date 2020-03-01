package com.app.belcobtm.data.rest.authorization

import com.app.belcobtm.data.rest.authorization.request.*
import com.app.belcobtm.data.rest.authorization.response.AddCoinsResponse
import com.app.belcobtm.data.rest.authorization.response.RecoverWalletResponse
import com.app.belcobtm.data.rest.authorization.response.RegisterWalletResponse
import kotlinx.coroutines.Deferred
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthApi {
    @POST("register")
    fun createWalletAsync(
        @Body request: CreateWalletRequest
    ): Deferred<Response<RegisterWalletResponse>>

    @POST("user/{userId}/code/verify")
    fun createWalletVerifySmsCodeAsync(
        @Path("userId") userId: Int,
        @Body request: CreateWalletVerifySmsCodeRequest
    ): Deferred<Response<ResponseBody>>

    @POST("recover")
    fun recoverWalletAsync(
        @Body request: RecoverWalletRequest
    ): Deferred<Response<RecoverWalletResponse>>

    @POST("user/{userId}/code/verify")
    fun recoverWalletVerifySmsCodeAsync(
        @Path("userId") userId: Int,
        @Body request: VerifySmsCodeRequest
    ): Deferred<Response<ResponseBody>>

    @POST("user/{userId}/coins/add")
    fun addCoinsAsync(
        @Path("userId") userId: Int,
        @Body request: AddCoinsRequest
    ): Deferred<Response<AddCoinsResponse>>
}