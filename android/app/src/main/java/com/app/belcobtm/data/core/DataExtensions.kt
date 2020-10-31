package com.app.belcobtm.data.core

import okhttp3.Response
import java.nio.charset.Charset


fun Response.getJSONFromBody(): String {
    val source = this.body()?.source()
    source?.request(Long.MAX_VALUE) // Buffer the entire body.
    return source?.buffer?.clone()?.readString(Charset.forName("UTF-8")).orEmpty()
}
