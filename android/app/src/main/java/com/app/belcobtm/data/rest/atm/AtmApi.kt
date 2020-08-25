package com.app.belcobtm.data.rest.atm

import com.app.belcobtm.api.model.ServerResponse
import com.app.belcobtm.api.model.response.AtmResponse
import io.reactivex.Observable
import retrofit2.http.GET

interface AtmApi {

    @GET("terminal/locations")
    fun getAtmAddress(): Observable<ServerResponse<AtmResponse>>
}