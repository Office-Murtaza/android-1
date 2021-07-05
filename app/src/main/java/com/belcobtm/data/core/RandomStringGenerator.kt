package com.belcobtm.data.core

class RandomStringGenerator {

    companion object {
        private val CHAR_POOL = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    }

    fun generate(size: Int): String =
        (1..size)
            .map { CHAR_POOL.random() }
            .joinToString("")
}