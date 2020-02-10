package com.app.belcobtm.domain

/**
 * Base Class for handling errors/failures/exceptions.
 * Every feature specific failure should extend [FeatureFailure] class.
 */
sealed class Failure {
    object NetworkConnection : Failure()
    object NotFound : Failure()
    object ServerError : Failure()
    object AuthorizationError : Failure()
    object BadRequest : Failure()
    object DataError : Failure()


    /** * Extend this class for feature specific failures.*/
    abstract class FeatureFailure : Failure()

}