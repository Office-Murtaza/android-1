package com.belcobtm.data.rest.unlink

import com.belcobtm.data.rest.unlink.response.UnlinkResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface UnlinkApi {

    @GET("user/{userId}/unlink")
    suspend fun unlinkAsync(
        @Path("userId") userId: String
    ): Response<UnlinkResponse>

}
