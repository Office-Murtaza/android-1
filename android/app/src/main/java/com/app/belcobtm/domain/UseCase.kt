package com.app.belcobtm.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

abstract class UseCase<out Type, in Params> where Type : Any {

    abstract suspend fun run(params: Params): Either<Failure, Type>

    operator fun invoke(
        params: Params,
        onSuccess: (Type) -> Unit,
        onError: (Failure) -> Unit
    ) {
        val job = CoroutineScope(Dispatchers.IO).async { run(params) }
        CoroutineScope(Dispatchers.Main).launch {
            val onResult: (Either<Failure, Type>) -> Unit = { either ->
                either.either(
                    { onError.invoke(it) },
                    { onSuccess.invoke(it) }
                )
            }
            onResult.invoke(job.await())
        }
    }

    class None
}