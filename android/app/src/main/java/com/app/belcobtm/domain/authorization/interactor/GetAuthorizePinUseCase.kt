package com.app.belcobtm.domain.authorization.interactor

import com.app.belcobtm.domain.authorization.AuthorizationRepository

data class GetAuthorizePinUseCase(private val repository: AuthorizationRepository) {
    operator fun invoke(): String = repository.getAuthorizePin()
}