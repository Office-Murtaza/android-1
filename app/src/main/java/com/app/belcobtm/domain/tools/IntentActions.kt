package com.app.belcobtm.domain.tools

import android.content.Context
import android.content.Intent
import android.net.Uri

interface IntentActions {
    fun openViewActivity(path: String)
}

class IntentActionsImpl(val appContext: Context) :
    IntentActions {
    override fun openViewActivity(path: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(path))
        .apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        intent.resolveActivity(appContext.packageManager)?.run {
            appContext.startActivity(intent)
        }
    }
}