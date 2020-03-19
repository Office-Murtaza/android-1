package com.app.belcobtm.data.rest.settings

import com.app.belcobtm.data.rest.settings.response.VerificationInfoResponse
import kotlinx.coroutines.Deferred
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface SettingsApi {
    @GET("user/{userId}/kyc")
    fun getVerificationInfoAsync(
        @Path("userId") userId: Int
    ): Deferred<Response<VerificationInfoResponse>>

    @Multipart
    @POST("user/{userId}/kyc")
    fun sendVerificationBlankAsync(
        @Path("userId") userId: Int,
        @Part("tierId") tierId: Int,
        @Part("idNumber") idNumber: String,
        @Part("firstName") firstName: String,
        @Part("lastName") lastName: String,
        @Part("address") address: String,
        @Part("city") city: String,
        @Part("country") country: String,
        @Part("province") province: String,
        @Part("zipCode") zipCode: String,
        @Part file: MultipartBody.Part
    ): Deferred<Response<ResponseBody>>

    @Multipart
    @POST("user/{userId}/kyc")
    fun sendVerificationVipAsync(
        @Path("userId") userId: Int,
        @Part("tierId") tierId: Int,
        @Part("ssn") ssn: Int,
        @Part file: MultipartBody.Part
    ): Deferred<Response<ResponseBody>>
}