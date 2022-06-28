package com.belcobtm.data.websockets.base.model

import androidx.annotation.StringDef

data class StompSocketRequest(
    @Command val command: String,
    val headers: Map<String, String>,
    val body: String = ""
) {
    companion object {
        @StringDef(CONNECT, SUBSCRIBE, MESSAGE)
        @Retention(AnnotationRetention.SOURCE)
        annotation class Command

        const val CONNECT = "CONNECT"
        const val SUBSCRIBE = "SUBSCRIBE"
        const val MESSAGE = "MESSAGE"
    }
}