package com.app.belcobtm.domain.wallet

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.item.BalanceDataItem
import kotlinx.coroutines.channels.Channel

interface WalletSocketRepository {
    suspend fun getBalanceChannel(): Channel<Either<Failure, BalanceDataItem>>
    fun unsubscribe()
    fun subscribe()
}