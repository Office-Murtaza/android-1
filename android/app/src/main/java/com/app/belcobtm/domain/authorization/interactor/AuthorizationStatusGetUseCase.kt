package com.app.belcobtm.domain.authorization.interactor

import com.app.belcobtm.domain.authorization.AuthorizationRepository
import com.app.belcobtm.domain.authorization.AuthorizationStatus

class AuthorizationStatusGetUseCase(private val repository: AuthorizationRepository) {
    operator fun invoke(): AuthorizationStatus = repository.getAuthorizationStatus()
}