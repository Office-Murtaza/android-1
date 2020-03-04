package com.app.belcobtm.data.rest.settings

import com.app.belcobtm.data.rest.settings.response.VerificationInfoResponse
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface SettingsApi {
    @GET("user/{userId}/kyc")
    fun getVerificationInfoAsync(
        @Path("userId") userId: Int
    ): Deferred<Response<VerificationInfoResponse>>
}