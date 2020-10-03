package com.app.belcobtm.data.rest.interceptor


import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.app.belcobtm.data.core.NetworkUtils
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.rest.ApiFactory
import com.app.belcobtm.data.rest.authorization.request.RefreshTokenRequest
import com.app.belcobtm.data.rest.authorization.response.AuthorizationResponse
import com.app.belcobtm.domain.Failure
import com.squareup.moshi.Moshi
import okhttp3.*
import org.json.JSONObject
import java.net.HttpURLConnection


class ResponseInterceptor(
    @Deprecated("Need change to simple listener")
    private val broadcastManager: LocalBroadcastManager,
    private val networkUtils: NetworkUtils,
    private val prefsHelper: SharedPreferencesHelper
) : Interceptor {

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
                            val newBody =
                                ResponseBody.create(it.contentType(), resultJson.toString())
                            return response.newBuilder().body(newBody).build()
                        }
                        response.isSuccessful && !JSONObject(json).isNull(ERROR_FIELD) -> {
                            val message = try {
                                JSONObject(json).getJSONObject(ERROR_FIELD)
                                    .getString(ERROR_SUB_FIELD)
                            } catch (e: Exception) {
                                null
                            }
                            val code = try {
                                JSONObject(json).getJSONObject(ERROR_FIELD)
                                    .getInt(ERROR_SUB_FIELD_CODE)
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
                    val refreshResponse = refreshToken(chain)
                    parseAuthResponse(refreshResponse)
                    val newResp = retryWithNewToken(chain, request)
                    if (newResp.code() == HttpURLConnection.HTTP_OK) {
                        newResp.let {
                            return proceedSuccessResponce(it)
                        }
                    } else {
                        val isUserUnauthorized =
                            request.url().encodedPath().equals(REQUEST_REFRESH_PATH, true)
                        val intent = Intent(TAG_USER_AUTHORIZATION)
                        intent.putExtra(KEY_IS_USER_UNAUTHORIZED, isUserUnauthorized)
                        broadcastManager.sendBroadcast(intent)
                        throw Failure.TokenError
                    }
                }
                else -> Unit
            }
            response
        } else {
            response
        }
    } catch (e: Exception) {
        if (networkUtils.isNetworkAvailable()) {
            throw Failure.ServerError(e.message)
        } else {
            throw Failure.NetworkConnection
        }
    }

    private fun retryWithNewToken(
        chain: Interceptor.Chain,
        request: Request
    ): Response {
        return chain.proceed(
            request.newBuilder()
                .removeHeader(BaseInterceptor.HEADER_AUTHORIZATION_KEY)
                .addHeader(
                    BaseInterceptor.HEADER_AUTHORIZATION_KEY,
                    prefsHelper.accessToken
                )
                .build()
        )
    }

    private fun parseAuthResponse(refreshResponse: Response): AuthorizationResponse? {
        val json = refreshResponse.body()?.string().orEmpty()
        val resultJson = JSONObject(json).get(RESPONSE_FIELD)
        return Moshi.Builder()
            .build()
            .adapter(AuthorizationResponse::class.java)
            .fromJson(resultJson.toString())
            ?.apply {
                prefsHelper.processAuthResponse(this)
            }
    }

    private fun refreshToken(
        chain: Interceptor.Chain
    ): Response {
        return chain.proceed(
            Request.Builder()
                .url("${ApiFactory.SERVER_URL}refresh")
                .method(
                    "POST", RequestBody
                        .create(
                            MediaType.get("application/json; charset=utf-8"),
                            Moshi.Builder()
                                .build().adapter(RefreshTokenRequest::class.java).toJson(
                                    RefreshTokenRequest(prefsHelper.refreshToken)
                                )
                        )
                )
                .build()
        )
    }

    private fun proceedSuccessResponce(response: Response): Response {
        return response.body()?.let {
            val json = it.string()
            when {
                response.isSuccessful && !JSONObject(json).isNull(RESPONSE_FIELD) -> {
                    val resultJson = JSONObject(json).get(RESPONSE_FIELD)
                    val newBody =
                        ResponseBody.create(it.contentType(), resultJson.toString())
                    response.newBuilder().body(newBody).build()
                }
                response.isSuccessful && !JSONObject(json).isNull(ERROR_FIELD) -> {
                    val message = try {
                        JSONObject(json).getJSONObject(ERROR_FIELD)
                            .getString(ERROR_SUB_FIELD)
                    } catch (e: Exception) {
                        null
                    }
                    val code = try {
                        JSONObject(json).getJSONObject(ERROR_FIELD)
                            .getInt(ERROR_SUB_FIELD_CODE)
                    } catch (e: Exception) {
                        null
                    }
                    throw Failure.MessageError(message, code)
                }
                else -> response
            }
        } ?: response
    }

    companion object {
        private const val RESPONSE_FIELD = "response"
        private const val ERROR_FIELD = "error"
        private const val ERROR_SUB_FIELD = "message"
        private const val ERROR_SUB_FIELD_CODE = "code"
        private const val REQUEST_REFRESH_PATH = "/api/v1/refresh"
        const val TAG_USER_AUTHORIZATION = "tag_broadcast_user_unauthorized"
        const val KEY_IS_USER_UNAUTHORIZED = "key_is_user_unauthorized"
    }
}