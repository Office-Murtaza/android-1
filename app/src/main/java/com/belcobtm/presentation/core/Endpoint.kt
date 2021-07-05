package com.belcobtm.presentation.core

import com.belcobtm.BuildConfig

object Endpoint {
    const val SERVER_URL = "${BuildConfig.BASE_URL}/api/v${BuildConfig.API_VERSION}/"
    const val SOCKET_URL = "${BuildConfig.BASE_URL}/api/v${BuildConfig.API_VERSION}/ws"
}
