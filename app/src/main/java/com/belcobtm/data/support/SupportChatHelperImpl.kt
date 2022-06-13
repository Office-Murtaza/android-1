package com.belcobtm.data.support

import android.content.Context
import android.util.Log
import com.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.belcobtm.domain.support.SupportChatHelper
import zendesk.android.FailureCallback
import zendesk.android.SuccessCallback
import zendesk.android.Zendesk
import zendesk.messaging.android.DefaultMessagingFactory

class SupportChatHelperImpl(
    private val context: Context,
    private val prefHelper: SharedPreferencesHelper
) : SupportChatHelper {

    private var zendesk: Zendesk? = null

    override fun init() {
        Zendesk.initialize(
            context = context,
            channelKey = CHANNEL_KEY,
            successCallback = getInitSuccessCallback(),
            failureCallback = getInitErrorCallback(),
            messagingFactory = DefaultMessagingFactory()
        )
    }

    private fun getInitSuccessCallback() = SuccessCallback<Zendesk> { instance ->
        zendesk = instance
        loginUser()
        Log.d("Zendesk", "Initialization is successful")
    }

    private fun loginUser() {
        val userIdentity = prefHelper.zendeskToken
        zendesk?.loginUser(
            jwt = userIdentity,
            successCallback = { _ ->
                Log.d("Zendesk", "Login is successful")
            },
            failureCallback = { error ->
                Log.e("Zendesk", "Login failed", error)
            }
        )
    }

    private fun getInitErrorCallback() = FailureCallback<Throwable> { error ->
        Log.e("Zendesk", "Initialization failed", error)
    }

    override fun reset() {
        zendesk?.logoutUser(
            successCallback = {
                Log.d("Zendesk", "Sign out is successful")
            },
            failureCallback = { error ->
                Log.e("Zendesk", "Sign out failed", error)
            }
        )
    }

    private companion object {

        private const val CHANNEL_KEY =
            "eyJzZXR0aW5nc191cmwiOiJodHRwczovL2F0dW01MDcxLnplbmRlc2suY29tL21vYmlsZV9zZGtfYXBpL3NldHRpbmdzLzAxRzNFWjVWS0tXNUJSUUtISFQxME05QUJNLmpzb24ifQ=="
    }

}
