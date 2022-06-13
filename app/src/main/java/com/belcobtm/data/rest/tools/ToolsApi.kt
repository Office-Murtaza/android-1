package com.belcobtm.data.rest.tools

import com.belcobtm.data.rest.authorization.request.VerifyPhoneRequest
import com.belcobtm.data.rest.authorization.response.VerifyPhoneResponse
import com.belcobtm.data.rest.transaction.request.VerifySmsCodeRequest
import com.belcobtm.data.rest.transaction.request.VerifySmsCodeRequestOld
import com.belcobtm.data.rest.transaction.response.SendSmsCodeResponse
import com.belcobtm.data.rest.transaction.response.VerifySmsCodeResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ToolsApi {

    @POST("send-code")
    suspend fun sendSmsAsync(
        @Body request: VerifyPhoneRequest
    ): Response<VerifyPhoneResponse>

    @GET("user/{userId}/code/send")
    suspend fun sendSmsCodeAsync(
        @Path("userId") userId: String
    ): Response<SendSmsCodeResponse>

    @POST("user/{userId}/code/verify")
    suspend fun verifySmsCodeAsyncOld(
        @Path("userId") userId: String,
        @Body verifySmsParamOld: VerifySmsCodeRequestOld
    ): Response<ResponseBody>

    @POST("check-code")
    suspend fun verifySmsCodeAsync(
        @Body verifySmsParam: VerifySmsCodeRequest
    ): Response<VerifySmsCodeResponse>

}
