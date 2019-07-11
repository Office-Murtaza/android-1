package com.app.belcobtm.api

import com.app.belcobtm.api.model.ServerResponse
import com.app.belcobtm.api.model.param.*
import com.app.belcobtm.api.model.response.AddCoinsResponse
import com.app.belcobtm.api.model.response.AuthResponse
import com.app.belcobtm.api.model.response.GetCoinsResponse
import com.app.belcobtm.api.model.response.VerifySmsResponse
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

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

    @POST("register")
    fun register(@Body registerParam: AuthParam): Observable<ServerResponse<AuthResponse>>

    @POST("recover")
    fun recover(@Body registerParam: AuthParam): Observable<ServerResponse<AuthResponse>>

    @POST("refresh")
    fun refresh(@Body refreshParam: RefreshParam): Observable<ServerResponse<AuthResponse>>

    @POST("user/login")
    fun login(@Body loginParam: AuthParam): Observable<ServerResponse<AuthResponse>>

    @POST("user/{userId}/verify")
    fun verifySmsCode(@Path("userId") userId: String, @Body verifySmsParam: VerifySmsParam): Observable<ServerResponse<VerifySmsResponse>>

    @POST("user/{userId}/coins/add")
    fun addCoins(@Path("userId") userId: String, @Body addCoinsParam: AddCoinsParam): Observable<ServerResponse<AddCoinsResponse>>

    @POST("user/{userId}/coins/compare")
    fun verifyCoins(@Path("userId") userId: String, @Body verifyCoinsParam: AddCoinsParam): Observable<ServerResponse<AddCoinsResponse>>

    @GET("user/{userId}/coins/balance")
    fun getCoins(@Path("userId") userId: String): Observable<ServerResponse<GetCoinsResponse>>//todo make right response


}