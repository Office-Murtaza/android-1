package com.app.belcobtm.domain.authorization.interactor


import com.app.belcobtm.domain.authorization.AuthorizationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ClearAppDataUseCase(private val repository: AuthorizationRepository) {

    operator fun invoke() {
        CoroutineScope(Dispatchers.Main).launch { repository.clearAppData() }
    }
}