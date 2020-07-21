package com.app.belcobtm.data.rest.interceptor


import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.app.belcobtm.domain.Failure
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection


class ResponseInterceptor(private val broadcastManager: LocalBroadcastManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response? {
        val request = chain.request()
        request.header("Content-Type: application/json")
        val response = chain.proceed(request)
        try {
            when (response.code()) {
                HttpURLConnection.HTTP_OK -> response.body()?.let {
                    val json = it.string()
                    when {
                        response.isSuccessful && !JSONObject(json).isNull(RESPONSE_FIELD) -> {
                            val resultJson = JSONObject(json).get(RESPONSE_FIELD)
                            val newBody = ResponseBody.create(it.contentType(), resultJson.toString())
                            return response.newBuilder().body(newBody).build()
                        }
                        response.isSuccessful && !JSONObject(json).isNull(ERROR_FIELD) -> {
                            val resultJson = JSONObject(json).getJSONObject(ERROR_FIELD).getString(ERROR_SUB_FIELD)
                            throw Failure.MessageError(resultJson.toString())
                        }
                        else -> Unit
                    }
                }
                HttpURLConnection.HTTP_NOT_FOUND -> throw Failure.ServerError("Not found")
                HttpURLConnection.HTTP_FORBIDDEN -> {
                    broadcastManager.sendBroadcast(Intent(TAG_USER_UNAUTHORIZED))
                    throw Failure.TokenError
                }
//                    response.newBuilder()
//                    .body(response.body())
//                    .code(HttpURLConnection.HTTP_UNAUTHORIZED)
//                    .build()
                else -> Unit
            }
        } catch (e: JSONException) {
            throw Failure.ServerError(e.message)
        }

        return response
    }

    companion object {
        private const val RESPONSE_FIELD = "response"
        private const val ERROR_FIELD = "error"
        private const val ERROR_SUB_FIELD = "errorMsg"
        private const val TAG_USER_UNAUTHORIZED = "tag_broadcast_user_unauthorized"
    }
}