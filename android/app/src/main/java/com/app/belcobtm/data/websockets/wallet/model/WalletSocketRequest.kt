package com.app.belcobtm.data.websockets.wallet.model

import androidx.annotation.StringDef

data class WalletSocketRequest(
    @Command val command: String,
    val headers: Map<String, String>,
    val body: String = ""
) {
    companion object {
        @StringDef()
        @Retention(AnnotationRetention.SOURCE)
        annotation class Command

        const val CONNECT = "CONNECT"
        const val SUBSCRIBE = "SUBSCRIBE"
        const val UNSUBSCRIBE = "UNSUBSCRIBE"
    }
}