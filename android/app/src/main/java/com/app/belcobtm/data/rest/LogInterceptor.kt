package com.app.belcobtm.data.rest

import android.util.Log
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import java.io.IOException
import java.nio.charset.Charset

class LogInterceptor : Interceptor {
    private val TAG = "HTTP_TRACE"
    private val UTF8 = Charset.forName("UTF-8")

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestBody = request.body()
        val logBuilder = StringBuilder()

        logBuilder.append("-- START REQUEST ").append(request.method()).append(" ").append(requestPath(request.url()))
            .append(" --")

        if (requestBody != null) {
            logBuilder.append("\n")
            logBuilder.append("Request:")
            logBuilder.append("\n")
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
                    .append("Response:")
                    .append("\n")
                    .append(buffer.clone().readString(charset!!))
            }
        }

        logBuilder
            .append("\n")
            .append("-- END REQUEST --")
        Log.d(TAG, logBuilder.toString())
        return response
    }

    private fun requestPath(url: HttpUrl): String {
        val path = url.encodedPath()
        val query = url.encodedQuery()
        return url.scheme() + "://" + url.host() + if (query != null) "$path?$query" else path
    }
}