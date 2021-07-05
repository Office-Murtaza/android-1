package com.belcobtm.data.rest.tools

import com.belcobtm.data.rest.authorization.request.VerifyPhoneRequest
import com.belcobtm.data.rest.authorization.response.VerifyPhoneResponse
import com.belcobtm.data.rest.transaction.request.VerifySmsCodeRequest
import com.belcobtm.data.rest.transaction.response.SendSmsCodeResponse
import kotlinx.coroutines.Deferred
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ToolsApi {
    @POST("verify")
    fun verifyPhoneAsync(
        @Body request: VerifyPhoneRequest
    ): Deferred<Response<VerifyPhoneResponse>>

    @GET("user/{userId}/code/send")
    fun sendSmsCodeAsync(
        @Path("userId") userId: String
    ): Deferred<Response<SendSmsCodeResponse>>

    @POST("user/{userId}/code/verify")
    fun verifySmsCodeAsync(
        @Path("userId") userId: String,
        @Body verifySmsParam: VerifySmsCodeRequest
    ): Deferred<Response<ResponseBody>>
}