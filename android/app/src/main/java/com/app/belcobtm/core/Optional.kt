package com.app.belcobtm.core

sealed class Optional<out T> {
    abstract val value: T?
}

class Some<out T>(override val value: T?) : Optional<T>() {
}

object None : Optional<Nothing>() {
    override val value: Nothing?
        get() = null
}