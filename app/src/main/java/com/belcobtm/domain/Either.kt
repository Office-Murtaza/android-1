package com.belcobtm.domain

sealed class Either<out L, out R> {
    /** * Represents the left side of [Either] class which by convention is a "Failure". */
    data class Left<out L>(val a: L) : Either<L, Nothing>()

    /** * Represents the right side of [Either] class which by convention is a "Success". */
    data class Right<out R>(val b: R) : Either<Nothing, R>()

    val isLeft get() = this is Left<L>
    val isRight get() = this is Right<R>

    fun <L> left(a: L) = Left(a)
    fun <R> right(b: R) = Right(b)

    fun either(fnL: (L) -> Unit, fnR: (R) -> Unit): Any = when (this) {
        is Left -> fnL(a)
        is Right -> fnR(b)
    }

    suspend fun eitherSuspend(fnL: suspend (L) -> Unit, fnR: suspend (R) -> Unit): Any = when (this) {
        is Left -> fnL(a)
        is Right -> fnR(b)
    }
}

// Credits to Alex Hart -> https://proandroiddev.com/kotlins-nothing-type-946de7d464fb
// Composes 2 functions
fun <A, B, C> ((A) -> B).c(f: (B) -> C): (A) -> C = {
    f(this(it))
}

fun <A, B, C> (suspend (A) -> B).c(f: (B) -> C): suspend (A) -> C = {
    f(this(it))
}

fun <T, L, R> Either<L, R>.flatMap(fn: (R) -> Either<L, T>): Either<L, T> =
    when (this) {
        is Either.Left -> Either.Left(a)
        is Either.Right -> fn(b)
    }

suspend fun <T, L, R> Either<L, R>.flatMapSuspend(fn: suspend (R) -> Either<L, T>): Either<L, T> =
    when (this) {
        is Either.Left -> Either.Left(a)
        is Either.Right -> fn(b)
    }

fun <T, L, R> Either<L, R>.map(fn: (R) -> (T)): Either<L, T> = this.flatMap(fn.c(::right))

suspend fun <T, L, R> Either<L, R>.mapSuspend(fn: suspend (R) -> (T)): Either<L, T> =
    flatMapSuspend(fn.c(::right))

fun Either<Failure, Any>.completable(): Either<Failure, UseCase.None> = when (isRight) {
    true -> Either.Right(UseCase.None())
    false -> this as Either.Left
}