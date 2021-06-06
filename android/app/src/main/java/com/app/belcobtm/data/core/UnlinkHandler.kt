package com.app.belcobtm.data.core

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.app.belcobtm.data.disk.database.account.AccountDao
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.rest.settings.SettingsApi
import com.app.belcobtm.data.websockets.wallet.WalletConnectionHandler
import com.app.belcobtm.presentation.features.HostActivity
import org.koin.core.KoinComponent
import org.koin.core.inject

class UnlinkHandler(
    private val prefsHelper: SharedPreferencesHelper,
    private val settingsApi: SettingsApi,
    private val daoAccount: AccountDao,
    private val context: Context
) : KoinComponent {

    private val connectionHandler: WalletConnectionHandler by inject()

    suspend fun performUnlink() {
        settingsApi.unlink(prefsHelper.userId.toString()).await()
        daoAccount.clearTable()
        prefsHelper.clearData()
        connectionHandler.disconnect()
        context.startActivity(Intent(context, HostActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtras(Bundle().apply {
                putBoolean(HostActivity.FORCE_UNLINK_KEY, true)
            })
        })
    }
}