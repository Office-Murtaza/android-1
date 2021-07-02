package com.app.belcobtm.data.websockets.serializer

interface ResponseDeserializer<R> {
    fun deserialize(content: String): R
}