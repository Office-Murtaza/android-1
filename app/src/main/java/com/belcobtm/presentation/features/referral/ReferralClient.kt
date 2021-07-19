package com.belcobtm.presentation.features.referral

import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ReferralClient(private val installReferrerClient: InstallReferrerClient) {

    suspend fun getReferralData(): String? = suspendCoroutine { continuation ->
        installReferrerClient.startConnection(object : InstallReferrerStateListener {
            var complete = false

            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                complete = true
                when (responseCode) {
                    InstallReferrerClient.InstallReferrerResponse.OK -> {
                        val details = installReferrerClient.installReferrer
                        val referrer = details.installReferrer.substringAfter("utm_content=")
                        continuation.resume(referrer)
                    }
                    else ->
                        continuation.resume(null)
                }
                installReferrerClient.endConnection()
            }

            override fun onInstallReferrerServiceDisconnected() {
                if (!complete) {
                    installReferrerClient.startConnection(this)
                }
            }
        })
    }
}