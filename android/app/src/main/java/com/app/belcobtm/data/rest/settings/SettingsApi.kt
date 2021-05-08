package com.app.belcobtm.data.rest.settings

import com.app.belcobtm.data.rest.settings.request.ChangePassBody
import com.app.belcobtm.data.rest.settings.request.UpdatePhoneParam
import com.app.belcobtm.data.rest.settings.request.VerificationBlankRequest
import com.app.belcobtm.data.rest.settings.request.VipVerificationRequest
import com.app.belcobtm.data.rest.settings.response.GetPhoneResponse
import com.app.belcobtm.data.rest.settings.response.UpdateResponse
import com.app.belcobtm.data.rest.settings.response.VerificationInfoResponse
import kotlinx.coroutines.Deferred
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface SettingsApi {
    @GET("user/{userId}/verification")
    fun getVerificationInfoAsync(
        @Path("userId") userId: Int
    ): Deferred<Response<VerificationInfoResponse>>

    @POST("user/{userId}/verification")
    fun sendVerificationBlankAsync(
        @Path("userId") userId: Int,
        @Body request: VerificationBlankRequest
    ): Deferred<Response<ResponseBody>>

    @POST("user/{userId}/verification")
    fun sendVerificationVipAsync(
        @Path("userId") userId: Int,
        @Body request: VipVerificationRequest
    ): Deferred<Response<ResponseBody>>

    @GET("user/{userId}/unlink")
    fun unlink(@Path("userId") userId: String): Deferred<Response<UpdateResponse>>

    @POST("user/{userId}/password")
    fun changePass(
        @Path("userId") userId: String,
        @Body changePassParam: ChangePassBody
    ): Deferred<Response<UpdateResponse>>

    @GET("user/{userId}/phone")
    fun getPhone(@Path("userId") userId: String): Deferred<Response<GetPhoneResponse>>

    @POST("user/{userId}/phone")
    fun updatePhone(
        @Path("userId") userId: String,
        @Body updatePhoneParam: UpdatePhoneParam
    ): Deferred<Response<UpdateResponse>>

    @POST("user/{userId}/phone-verify")
    fun verifyPhone(
        @Path("userId") userId: String,
        @Body updatePhoneParam: UpdatePhoneParam
    ): Deferred<Response<UpdateResponse>>
}