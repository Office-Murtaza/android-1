package com.app.belcobtm.domain

import kotlinx.coroutines.*

abstract class UseCase<out Type, in Params> where Type : Any {

    abstract suspend fun run(params: Params): Either<Failure, Type>

    operator fun invoke(params: Params, onResult: (Either<Failure, Type>) -> Unit = {}) {
        val job = CoroutineScope(Dispatchers.IO).async { run(params) }
        CoroutineScope(Dispatchers.Main).launch { onResult(job.await()) }
    }

    class None
}