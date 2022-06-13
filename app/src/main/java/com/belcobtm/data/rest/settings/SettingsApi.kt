package com.belcobtm.data.rest.settings

import com.belcobtm.data.rest.settings.request.ChangePassBody
import com.belcobtm.data.rest.settings.request.UpdatePhoneParam
import com.belcobtm.data.rest.settings.request.VerificationBlankRequest
import com.belcobtm.data.rest.settings.request.VerificationDocumentRequest
import com.belcobtm.data.rest.settings.request.VerificationUserIdentityRequest
import com.belcobtm.data.rest.settings.request.VipVerificationRequest
import com.belcobtm.data.rest.settings.response.GetPhoneResponse
import com.belcobtm.data.rest.settings.response.UpdateResponse
import com.belcobtm.data.rest.settings.response.VerificationDetailsResponse
import com.belcobtm.data.rest.settings.response.VerificationFieldsResponse
import com.belcobtm.data.rest.settings.response.VerificationInfoResponse
import com.belcobtm.data.rest.settings.response.VerificationUserDocumentResponse
import com.belcobtm.data.rest.settings.response.VerificationUserIdentityResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface SettingsApi {

    @GET("user/{userId}/verification")
    suspend fun getVerificationInfoAsync(
        @Path("userId") userId: String
    ): Response<VerificationInfoResponse>

    @GET("verification/user/{userId}/details")
    suspend fun getVerificationDetailsAsync(
        @Path("userId") userId: String
    ): Response<VerificationDetailsResponse>

    @GET("verification/{countryCode}/fields")
    suspend fun getVerificationFieldsAsync(
        @Path("countryCode") countryCode: String
    ): Response<VerificationFieldsResponse>

    @POST("verification/user/{userId}/identity")
    suspend fun sendVerificationIdentityAsync(
        @Path("userId") userId: String,
        @Body request: VerificationUserIdentityRequest
    ): Response<VerificationUserIdentityResponse>

    @POST("verification/user/{userId}/document")
    suspend fun sendVerificationDocumentAsync(
        @Path("userId") userId: String,
        @Body request: VerificationDocumentRequest
    ): Response<VerificationUserDocumentResponse>

    @POST("user/{userId}/verification")
    suspend fun sendVerificationBlankAsync(
        @Path("userId") userId: String,
        @Body request: VerificationBlankRequest
    ): Response<ResponseBody>

    @POST("user/{userId}/verification")
    suspend fun sendVerificationVipAsync(
        @Path("userId") userId: String,
        @Body request: VipVerificationRequest
    ): Response<ResponseBody>

    @POST("user/{userId}/password")
    suspend fun changePass(
        @Path("userId") userId: String,
        @Body changePassParam: ChangePassBody
    ): Response<UpdateResponse>

    @GET("user/{userId}/phone")
    suspend fun getPhone(
        @Path("userId") userId: String
    ): Response<GetPhoneResponse>

    @POST("user/{userId}/phone")
    suspend fun updatePhone(
        @Path("userId") userId: String,
        @Body updatePhoneParam: UpdatePhoneParam
    ): Response<UpdateResponse>

    @POST("user/{userId}/phone-verify")
    suspend fun verifyPhone(
        @Path("userId") userId: String,
        @Body updatePhoneParam: UpdatePhoneParam
    ): Response<UpdateResponse>

}
