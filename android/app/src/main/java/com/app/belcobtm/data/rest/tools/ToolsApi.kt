package com.app.belcobtm.data.rest.tools

import com.app.belcobtm.data.rest.transaction.request.VerifySmsCodeRequest
import com.app.belcobtm.data.rest.transaction.response.SendSmsCodeResponse
import kotlinx.coroutines.Deferred
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ToolsApi {
    @GET("user/{userId}/code/send")
    fun sendSmsCodeAsync(
        @Path("userId") userId: Int
    ): Deferred<Response<SendSmsCodeResponse>>

    @POST("user/{userId}/code/verify")
    fun verifySmsCodeAsync(
        @Path("userId") userId: Int,
        @Body verifySmsParam: VerifySmsCodeRequest
    ): Deferred<Response<ResponseBody>>
}