package com.app.belcobtm.domain

import java.io.IOException

/**
 * Base Class for handling errors/failures/exceptions.
 * Every feature specific failure should extend [FeatureFailure] class.
 */
sealed class Failure : IOException() {
    object NetworkConnection : Failure()
    object XRPLowAmountToSend : Failure()
    object OperationCannotBePerformed : Failure()

    @Deprecated("This is old realisation for activities. In [data.rest.interceptor.ResponseInterceptor] you can find broadcast that send event for [presentation.features.HostActivity].")
    object TokenError : Failure()

    data class ServerError(override val message: String? = null) : Failure()
    data class MessageError(override val message: String?, val code: Int? = null) : Failure()
    data class ValidationError(override val message: String? = null) : Failure()
    data class ClientValidationError(override val message: String? = null) : Failure()

    data class WalletFetchError(override val message: String? = null) : Failure()
}
