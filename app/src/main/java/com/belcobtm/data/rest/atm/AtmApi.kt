package com.belcobtm.data.rest.atm

import com.belcobtm.data.rest.atm.response.AtmResponse
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET

interface AtmApi {

    @GET("terminal/locations")
    fun getAtmAddress(): Deferred<Response<AtmResponse>>
}