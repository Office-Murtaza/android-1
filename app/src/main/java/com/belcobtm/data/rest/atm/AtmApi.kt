package com.belcobtm.data.rest.atm

import com.belcobtm.data.rest.atm.response.AtmAddress
import retrofit2.Response
import retrofit2.http.GET

interface AtmApi {

    @GET("locations")
    suspend fun getAtmAddress(): Response<List<AtmAddress>>

}
