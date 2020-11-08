package com.app.belcobtm.data.websockets.serializer

interface RequestSerializer<R> {
    fun serialize(request: R): String
}