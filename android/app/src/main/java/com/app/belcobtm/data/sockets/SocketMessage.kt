package com.app.belcobtm.data.sockets

import java.util.*

class SocketMessage(var command: String? = null) {
    val headers: MutableMap<String, String> = HashMap()
    var body = ""

    fun getHeader(name: String?): String? {
        return headers[name]
    }

    fun put(name: String, value: String) {
        headers[name] = value
    }



}