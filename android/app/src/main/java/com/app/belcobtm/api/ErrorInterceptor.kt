package com.app.belcobtm.api

import com.app.belcobtm.App
import com.app.belcobtm.api.model.ServerException
import com.app.belcobtm.api.model.ServerResponse
import com.app.belcobtm.data.core.NetworkUtils
import com.app.belcobtm.domain.Failure
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import okhttp3.Interceptor
import okhttp3.Response
import java.net.UnknownHostException

@Deprecated("Old realization")
class ErrorInterceptor : Interceptor {
    private val networkUtils = NetworkUtils(App.appContext())
    override fun intercept(chain: Interceptor.Chain): Response? {
        if (networkUtils.isNetworkAvailable()) {
            val response = try {
                chain.proceed(chain.request())
            } catch (e: Exception) {
                e.printStackTrace()
                throw Exception("Something went wrong. Please try again later")
            }

            response?.let {
                if (!response.isSuccessful) try {
                    if (response.code() == 403) {
                        throw ServerException(response.code(), "Refresh token")
                    }
                    if (response.code() == 500) {
                        throw ServerException(response.code(), "Server Error, Try Later")
                    }

                    val responseBody = response.body()?.string()
                    val serverResponse = Gson().fromJson(responseBody, ServerResponse::class.java)

                    throw ServerException(serverResponse.error?.errorCode, serverResponse.error?.errorMsg)
                } catch (e: JsonSyntaxException) {
//            Crashlytics.logException(e)
                    throw e
                }
            }

            return response
        } else {
            throw ServerException(0, "No Internet Connection")
        }
    }
}
