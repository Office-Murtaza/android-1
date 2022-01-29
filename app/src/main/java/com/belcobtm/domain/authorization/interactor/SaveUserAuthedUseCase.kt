package com.belcobtm.domain.authorization.interactor

import com.belcobtm.domain.authorization.AuthorizationRepository

data class SaveUserAuthedUseCase(private val repository: AuthorizationRepository) {
    operator fun invoke(params: Params) = repository.setIsUserAuthed(params.isAuthed)

    data class Params(val isAuthed: Boolean)
}