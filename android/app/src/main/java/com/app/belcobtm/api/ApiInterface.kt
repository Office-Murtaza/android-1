package com.app.belcobtm.api

import com.app.belcobtm.api.model.ServerResponse
import com.app.belcobtm.api.model.param.AddCoinsParam
import com.app.belcobtm.api.model.param.RegisterParam
import com.app.belcobtm.api.model.param.VerifySmsParam
import com.app.belcobtm.api.model.response.AddCoinsResponse
import com.app.belcobtm.api.model.response.RegisterResponse
import com.app.belcobtm.api.model.response.VerifySmsResponse
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Created by ADMIN on 17.07.2018.
 */
interface ApiInterface {

//    @FormUrlEncoded
//    @PUT("auth/resetPasswordRequests/{token}")
//    fun resetPassword(@Path("token") token: String?, @Field("password") pass: String): Observable<ServerResponse<Any>>
//
//    @GET("flights_v2")
//    fun findFlight(@Query("code") flightCode: String, @Query("departure") date: String): Observable<ServerResponse<ArrayList<FlightModel>>>
//
//    @POST("orders/{id}/accept")
//    fun orderInsurance(@Path("id") insuranceId: String?): Observable<ServerResponse<FlightModel>>
//
//    @GET("orders")
//    fun getInsurances(): Observable<ServerResponse<ArrayList<FlightModel>>>
//
//    @DELETE("orders/{id}")
//    fun deleteInsurance(@Path("id") insuranceId: String?): Observable<ServerResponse<Any>>

//    @Multipart
//    @POST("account/verification")
//    fun verification12(@Part application_level: MultipartBody.Part?,
//                       @Part document_type: MultipartBody.Part?,
//                       @Part document_front: MultipartBody.Part?,
//                       @Part document_back: MultipartBody.Part?): Observable<ServerResponse<Any>>

    @POST("user/register")
    fun register(@Body registerParam: RegisterParam): Observable<ServerResponse<RegisterResponse>>

    @POST("user/recover")
    fun recover(@Body registerParam: RegisterParam): Observable<ServerResponse<RegisterResponse>>

    @POST("user/verify")
    fun verifySmsCode(@Body verifySmsParam: VerifySmsParam): Observable<ServerResponse<VerifySmsResponse>>

    @POST("user/add-coins")
    fun addCoins(@Body addCoinsParam: AddCoinsParam): Observable<ServerResponse<AddCoinsResponse>>





}