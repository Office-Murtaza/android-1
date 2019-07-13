package com.app.belcobtm.api

import com.app.belcobtm.api.model.ServerException
import com.app.belcobtm.api.model.ServerResponse
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import okhttp3.Interceptor
import okhttp3.Response


class ErrorInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (!response.isSuccessful) try {
            if(response.code() == 403){
                throw ServerException(response.code(), "Refresh token")
            }
            val responseBody = response.body()?.string()
            val serverResponse = Gson().fromJson(responseBody, ServerResponse::class.java)

            throw ServerException(serverResponse.error?.errorCode, serverResponse.error?.errorMsg)
        } catch (e: JsonSyntaxException) {
//            Crashlytics.logException(e)
            throw e
        }
        return response
    }
}
