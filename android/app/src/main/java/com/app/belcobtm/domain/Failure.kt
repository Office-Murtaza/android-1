package com.app.belcobtm.domain

import java.io.IOException

/**
 * Base Class for handling errors/failures/exceptions.
 * Every feature specific failure should extend [FeatureFailure] class.
 */
sealed class Failure : IOException() {
    object NetworkConnection : Failure()
    object TokenError : Failure()

    //Authorization
    object IncorrectPassword : Failure()

    data class ServerError(override val message: String? = null) : Failure()
    data class MessageError(override val message: String?) : Failure()
}
