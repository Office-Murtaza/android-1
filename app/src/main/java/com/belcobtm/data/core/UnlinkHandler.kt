package com.belcobtm.data.core

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.belcobtm.data.disk.database.account.AccountDao
import com.belcobtm.data.disk.database.wallet.WalletDao
import com.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.belcobtm.data.rest.unlink.UnlinkApi
import com.belcobtm.domain.support.SupportChatHelper
import com.belcobtm.presentation.screens.HostActivity
import org.koin.core.component.KoinComponent

class UnlinkHandler(
    private val prefsHelper: SharedPreferencesHelper,
    private val daoAccount: AccountDao,
    private val walletDao: WalletDao,
    private val context: Context,
    private val unlinkApi: UnlinkApi,
    private val supportChatHelper: SupportChatHelper
) : KoinComponent {

    suspend fun performUnlink(openInitialScreen: Boolean = true) {
        daoAccount.clearTable()
        walletDao.clear()
        unlink()
        prefsHelper.clearData()
        supportChatHelper.reset()
        if (openInitialScreen) {
            context.startActivity(Intent(context, HostActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtras(Bundle().apply {
                    putBoolean(HostActivity.FORCE_UNLINK_KEY, true)
                })
            })
        }
    }

    private suspend fun unlink() {
        try {
            if (prefsHelper.userId.isNotEmpty()) {
                unlinkApi.unlinkAsync(prefsHelper.userId)
            }
        } catch (e: Exception) {
        }
    }

}
