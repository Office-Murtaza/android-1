package com.app.belcobtm.presentation.core

import com.app.belcobtm.BuildConfig

object Const {
    const val SERVER_URL = "${BuildConfig.BASE_URL}/api/v${BuildConfig.API_VERSION}/"
    const val SOCKET_URL = "${BuildConfig.BASE_URL}/api/v${BuildConfig.API_VERSION}/ws"
    const val TERMS_URL = "https://www.belcobtm.com/terms-and-conditions "
    const val SUPPORT_URL = "https://www.belcobtm.com/contact-us"
    const val GIPHY_API_KEY = "8IEBjOFUS31WQY6zK2ryvf9xMCxSYTpM"
    const val MIN_PASS = 6
    const val MAX_PASS = 15
}