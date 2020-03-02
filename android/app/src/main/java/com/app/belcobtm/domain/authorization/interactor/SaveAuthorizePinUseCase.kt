package com.app.belcobtm.domain.authorization.interactor

import com.app.belcobtm.domain.authorization.AuthorizationRepository

data class SaveAuthorizePinUseCase(private val repository: AuthorizationRepository) {
    operator fun invoke(params: Params) = repository.setAuthorizePin(params.pinCode)

    data class Params(val pinCode: String)
}