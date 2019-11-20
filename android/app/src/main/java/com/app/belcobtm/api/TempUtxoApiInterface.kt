package com.app.belcobtm.api

import com.app.belcobtm.api.model.ServerResponse
import com.app.belcobtm.api.model.param.*
import com.app.belcobtm.api.model.response.*
import io.reactivex.Observable
import retrofit2.http.*


interface TempUtxoApiInterface {

//    http://167.99.144.115:9134/api/v2/utxo/zpub6rQXovHLrRTUe26V1LxbdJTs2qJ9tmC3eEVmjcRa2s6muHuDJu4hjV46TDASVTpXeEFborJsVdomj9WLDH5odp2Wz71WmPhRcdCPmrvQ7sM
    @GET("utxo/{publicKey}")
    fun getUtxo(@Path("publicKey") publicKey: String): Observable<ArrayList<UtxoItem>>



}