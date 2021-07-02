package com.app.belcobtm.data.rest.interceptor

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
        if (e !is Failure) {
            throw Failure.ServerError(e.message)
        } else {
            throw e
        }
    }

    private fun extractResponse(response: Response): Response {
        val body = response.body() ?: return response
        val json = getJSONFromBody(body)
        val jsonObject = JSONObject(json)
        return when {
            response.isSuccessful && !jsonObject.isNull(RESPONSE_FIELD) -> {
                val resultJson = jsonObject.get(RESPONSE_FIELD)
                val newBody =
                    ResponseBody.create(body.contentType(), resultJson.toString())
                response.newBuilder().body(newBody).build()
            }
            response.isSuccessful && !jsonObject.isNull(ERROR_FIELD) -> {
                val errorJsonObject = jsonObject.getJSONObject(ERROR_FIELD)
                val message = try {
                    errorJsonObject.getString(ERROR_SUB_FIELD)
                } catch (e: Exception) {
                    null
                }
                val code = try {
                    errorJsonObject.getInt(ERROR_SUB_FIELD_CODE)
                } catch (e: Exception) {
                    null
                }
                throw Failure.MessageError(message, code)
            }
            else -> response
        }
    }

    private fun getJSONFromBody(body: ResponseBody): String {
        val source = body.source()
        source.request(Long.MAX_VALUE) // Buffer the entire body.
        return source.buffer.clone().readString(Charset.forName("UTF-8"))
    }

    companion object {
        private const val RESPONSE_FIELD = "response"
        private const val ERROR_FIELD = "error"
        private const val ERROR_SUB_FIELD = "message"
        private const val ERROR_SUB_FIELD_CODE = "code"
    }
}