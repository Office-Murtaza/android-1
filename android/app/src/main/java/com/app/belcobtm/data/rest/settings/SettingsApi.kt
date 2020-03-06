package com.app.belcobtm.data.rest.settings

import com.app.belcobtm.data.rest.settings.response.VerificationInfoResponse
import kotlinx.coroutines.Deferred
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface SettingsApi {
    @GET("user/{userId}/kyc")
    fun getVerificationInfoAsync(
        @Path("userId") userId: Int
    ): Deferred<Response<VerificationInfoResponse>>

    fun sendVerificationBlank(
        @Part("tierId") tierId: RequestBody,
        @Part("file") file: RequestBody,
        @Part("idNumber") idNumber: RequestBody,
        @Part("snn") snn: RequestBody,
        @Part("firstName") firstName: RequestBody,
        @Part("lastName") lastName: RequestBody,
        @Part("address") address: RequestBody,
        @Part("city") city: RequestBody,
        @Part("country") country: RequestBody,
        @Part("province") province: RequestBody,
        @Part("zipCode") zipCode: RequestBody
    )
}