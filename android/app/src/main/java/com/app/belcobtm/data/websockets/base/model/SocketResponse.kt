package com.app.belcobtm.data.websockets.base.model

import androidx.annotation.IntDef
import com.app.belcobtm.domain.Either

sealed class SocketResponse {
    data class Status(@Code val code: Int) : SocketResponse() {
        companion object {

            @IntDef(OPENED, DISCONNECTED, FAILURE)
            @Retention(AnnotationRetention.SOURCE)
            annotation class Code

            const val OPENED = 1
            const val DISCONNECTED = 2
            const val FAILURE = 3
        }
    }

    data class Message(val content: Either<String, Throwable>) : SocketResponse()
}