package com.belcobtm.data.rest.unlink

import com.belcobtm.data.rest.unlink.response.UnlinkResponse
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface UnlinkApi {

    @GET("user/{userId}/unlink")
    fun unlinkAsync(
        @Path("userId") userId: String
    ): Deferred<Response<UnlinkResponse>>
}