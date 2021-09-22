package com.belcobtm.domain.authorization.interactor


import com.belcobtm.domain.authorization.AuthorizationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ClearAppDataUseCase(private val repository: AuthorizationRepository) {

    // TODO fix issue with thread
    operator fun invoke() {
        CoroutineScope(Dispatchers.Main).launch { repository.clearAppData() }
    }
}