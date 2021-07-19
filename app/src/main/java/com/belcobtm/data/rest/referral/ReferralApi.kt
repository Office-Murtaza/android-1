package com.belcobtm.data.rest.referral

import com.belcobtm.data.rest.referral.request.GetExistedPhonesRequest
import com.belcobtm.data.rest.referral.response.ExistedPhonesResponse
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ReferralApi {

    @POST("existing-phones")
    fun getExistedPhonesAsync(
        @Body body: GetExistedPhonesRequest
    ): Deferred<Response<ExistedPhonesResponse>>
}