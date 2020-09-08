package com.app.belcobtm.data.rest.settings

import com.app.belcobtm.data.rest.settings.request.ChangePassBody
import com.app.belcobtm.data.rest.settings.response.GetPhoneResponse
import com.app.belcobtm.data.rest.settings.request.UpdatePhoneParam
import com.app.belcobtm.data.rest.settings.response.UpdateResponse
import com.app.belcobtm.data.rest.settings.response.VerificationInfoResponse
import kotlinx.coroutines.Deferred
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface SettingsApi {
    @GET("user/{userId}/kyc-details")
    fun getVerificationInfoAsync(
        @Path("userId") userId: Int
    ): Deferred<Response<VerificationInfoResponse>>

    @Multipart
    @POST("user/{userId}/kyc-submit")
    fun sendVerificationBlankAsync(
        @Path("userId") userId: Int,
        @Part("tierId") tierId: Int,
        @Part("idNumber") idNumber: RequestBody,
        @Part("firstName") firstName: RequestBody,
        @Part("lastName") lastName: RequestBody,
        @Part("address") address: RequestBody,
        @Part("city") city: RequestBody,
        @Part("country") country: RequestBody,
        @Part("province") province: RequestBody,
        @Part("zipCode") zipCode: RequestBody,
        @Part file: MultipartBody.Part
    ): Deferred<Response<ResponseBody>>

    @Multipart
    @POST("user/{userId}/kyc-submit")
    fun sendVerificationVipAsync(
        @Path("userId") userId: Int,
        @Part("tierId") tierId: Int,
        @Part("ssn") ssn: Int,
        @Part file: MultipartBody.Part
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