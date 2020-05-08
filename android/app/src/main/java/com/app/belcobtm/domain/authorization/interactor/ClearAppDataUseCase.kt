package com.app.belcobtm.domain.authorization.interactor


import com.app.belcobtm.domain.authorization.AuthorizationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class ClearAppDataUseCase(private val repository: AuthorizationRepository) {

    operator fun invoke(onResult: (Unit) -> Unit) {
        val job = CoroutineScope(Dispatchers.IO).async { repository.clearAppData() }
        CoroutineScope(Dispatchers.Main).launch { onResult(job.await()) }
    }
}