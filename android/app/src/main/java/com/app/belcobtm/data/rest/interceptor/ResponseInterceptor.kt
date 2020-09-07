package com.app.belcobtm.data.rest.interceptor


import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.app.belcobtm.data.core.NetworkUtils
import com.app.belcobtm.domain.Failure
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection


class ResponseInterceptor(
    private val broadcastManager: LocalBroadcastManager,
    private val networkUtils: NetworkUtils
) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response? = try {
        val request = chain.request()
        request.header("Content-Type: application/json")
        val response: Response = chain.proceed(request)
        if (!request.url().url().toString().contains("/ws")) {
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
                            val message = try {
                                JSONObject(json).getJSONObject(ERROR_FIELD).getString(ERROR_SUB_FIELD)
                            } catch (e: Exception) {
                                null
                            }
                            val code = try {
                                JSONObject(json).getJSONObject(ERROR_FIELD).getInt(ERROR_SUB_FIELD_CODE)
                            } catch (e: Exception) {
                                null
                            }
                            throw Failure.MessageError(message, code)
                        }
                        else -> Unit
                    }
                }
                HttpURLConnection.HTTP_NOT_FOUND -> throw Failure.ServerError("Not found")
                HttpURLConnection.HTTP_FORBIDDEN -> {
                    val isUserUnauthorized = request.url().encodedPath().equals(REQUEST_REFRESH_PATH, true)
                    val intent = Intent(TAG_USER_AUTHORIZATION)
                    intent.putExtra(KEY_IS_USER_UNAUTHORIZED, isUserUnauthorized)
                    broadcastManager.sendBroadcast(intent)
                    throw Failure.TokenError
                }
                else -> Unit
            }
            response
        } else {
            response
        }
    } catch (e: JSONException) {
        if (networkUtils.isNetworkAvailable()) {
            throw Failure.ServerError(e.message)
        } else {
            throw Failure.NetworkConnection
        }
    }

    companion object {
        private const val RESPONSE_FIELD = "response"
        private const val ERROR_FIELD = "error"
        private const val ERROR_SUB_FIELD = "message"
        private const val ERROR_SUB_FIELD_CODE = "code"
        private const val REQUEST_REFRESH_PATH = "/api/v1/refresh"
        private const val TAG_USER_AUTHORIZATION = "tag_broadcast_user_unauthorized"
        private const val KEY_IS_USER_UNAUTHORIZED = "key_is_user_unauthorized"
    }
}