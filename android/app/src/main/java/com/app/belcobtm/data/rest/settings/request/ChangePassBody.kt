package com.app.belcobtm.data.rest.settings.request

data class ChangePassBody(
    val newPassword: String,
    val oldPassword: String
)