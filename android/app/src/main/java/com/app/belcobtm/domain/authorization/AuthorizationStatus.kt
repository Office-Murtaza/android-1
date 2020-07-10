package com.app.belcobtm.domain.authorization

enum class AuthorizationStatus {
    UNAUTHORIZED,
    AUTHORIZED,
    SEED_PHRASE_CREATE,
    SEED_PHRASE_ENTER,
    PIN_CODE_CREATE,
    PIN_CODE_ENTER
}