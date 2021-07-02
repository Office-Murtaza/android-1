package com.app.belcobtm.data.core

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.app.belcobtm.data.disk.database.account.AccountDao
import com.app.belcobtm.data.disk.database.wallet.WalletDao
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.rest.settings.SettingsApiService
import com.app.belcobtm.data.websockets.wallet.WalletConnectionHandler
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.map
import com.app.belcobtm.presentation.features.HostActivity
import org.koin.core.KoinComponent
import org.koin.core.inject

class UnlinkHandler(
    private val prefsHelper: SharedPreferencesHelper,
    private val daoAccount: AccountDao,
    private val walletDao: WalletDao,
    private val context: Context
) : KoinComponent {

    private val connectionHandler: WalletConnectionHandler by inject()

    suspend fun performUnlink(openInitialScreen: Boolean = true) {
        daoAccount.clearTable()
        walletDao.clear()
        prefsHelper.clearData()
        connectionHandler.disconnect()
        if (openInitialScreen) {
            context.startActivity(Intent(context, HostActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtras(Bundle().apply {
                    putBoolean(HostActivity.FORCE_UNLINK_KEY, true)
                })
            })
        }
    }
}