package com.app.belcobtm.data.rest.interceptor

import android.util.Log
import com.app.belcobtm.data.core.NetworkUtils
import com.app.belcobtm.domain.Failure
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import java.io.IOException
import java.nio.charset.Charset

class LogInterceptor(
    private val networkUtils: NetworkUtils
) : Interceptor {
    private val TAG = "HTTP_TRACE"
    private val UTF8 = Charset.forName("UTF-8")

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response? = try {
        val request = chain.request()
        val requestBody = request.body()
        val logBuilder = StringBuilder()

        logBuilder
            .append(" \n-- START REQUEST ")
            .append(request.method())
            .append(" ")
            .append(requestPath(request.url()))
            .append(" --")

        if (requestBody != null) {
            logBuilder.append("\n")
            logBuilder.append("Request: ")
            val buffer = Buffer()
            requestBody.writeTo(buffer)

            val contentType = requestBody.contentType()
            contentType?.charset(UTF8)

            logBuilder.append(buffer.readString(UTF8))
        }


        val response = chain.proceed(request)
        val responseBody = response.body()

        if (responseBody != null) {

            val source = responseBody.source()
            source.request(java.lang.Long.MAX_VALUE) // Buffer the entire body.
            val buffer = source.buffer()

            var charset: Charset? = null
            val contentType = responseBody.contentType()
            if (contentType != null) {
                charset = contentType.charset(UTF8)
            }

            if (charset == null) {
                charset = UTF8
            }

            if (responseBody.contentLength() != 0L) {
                logBuilder
                    .append("\n")
                    .append("Response: ")
                    .append(buffer.clone().readString(charset!!))
            }
        }

        logBuilder
            .append("\n")
            .append("-- END REQUEST --")
        Log.d(TAG, logBuilder.toString())
        response
    } catch (e: Exception) {
        if (networkUtils.isNetworkAvailable()) {
            throw e
        } else {
            throw Failure.NetworkConnection
        }
    }

    private fun requestPath(url: HttpUrl): String {
        val path = url.encodedPath()
        val query = url.encodedQuery()
        return url.scheme() + "://" + url.host() + if (query != null) "$path?$query" else path
    }
}