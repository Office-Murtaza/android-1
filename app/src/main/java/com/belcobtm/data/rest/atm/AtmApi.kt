package com.belcobtm.data.rest.atm

import com.belcobtm.data.rest.atm.response.AtmAddress
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET

interface AtmApi {

    @GET("locations")
    fun getAtmAddress(): Deferred<Response<List<AtmAddress>>>
}