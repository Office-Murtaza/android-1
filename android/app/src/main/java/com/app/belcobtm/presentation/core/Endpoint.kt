package com.app.belcobtm.presentation.core

import com.app.belcobtm.BuildConfig

object Endpoint {
    const val SERVER_URL = "${BuildConfig.BASE_URL}/api/v${BuildConfig.API_VERSION}/"
}
