package com.belcobtm.domain.authorization.interactor

import com.belcobtm.domain.authorization.AuthorizationRepository
import com.belcobtm.domain.authorization.AuthorizationStatus

class AuthorizationStatusGetUseCase(private val repository: AuthorizationRepository) {
    operator fun invoke(): AuthorizationStatus = repository.getAuthorizationStatus()
}