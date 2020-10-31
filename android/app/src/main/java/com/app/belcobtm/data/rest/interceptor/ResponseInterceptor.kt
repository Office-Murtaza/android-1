package com.app.belcobtm.data.rest.interceptor


import com.app.belcobtm.data.core.getJSONFromBody
import com.app.belcobtm.domain.Failure
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import org.json.JSONObject
import java.net.HttpURLConnection
import java.nio.charset.Charset


class ResponseInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response = try {
        val request = chain.request()
        val response = chain.proceed(request)
        if (!request.url().url().toString().contains("/ws")) {
            if (response.code() == HttpURLConnection.HTTP_OK) {
                extractResponse(response)
            } else {
                response
            }
        } else {
            response
        }
    } catch (e: Exception) {
        throw Failure.ServerError(e.message)
    }

    fun extractResponse(response: Response): Response {
        val body = response.body() ?: return response
        val json = response.getJSONFromBody()
        return when {
            response.isSuccessful && !JSONObject(json).isNull(RESPONSE_FIELD) -> {
                val resultJson = JSONObject(json).get(RESPONSE_FIELD)
                val newBody =
                    ResponseBody.create(body.contentType(), resultJson.toString())
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
    }

    companion object {
        private const val RESPONSE_FIELD = "response"
        private const val ERROR_FIELD = "error"
        private const val ERROR_SUB_FIELD = "message"
        private const val ERROR_SUB_FIELD_CODE = "code"
    }
}