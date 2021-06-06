package com.app.belcobtm.data.disk.database.wallet

import androidx.room.Embedded
import com.app.belcobtm.data.disk.database.account.AccountEntity
import com.app.belcobtm.domain.wallet.item.CoinDataItem

data class FullCoinEntity(
    @Embedded val coin: CoinEntity,
    @Embedded val coinDetails: CoinDetailsEntity,
    @Embedded val accountEntity: AccountEntity
)

fun FullCoinEntity.toDataItem() = CoinDataItem(
    balanceCoin = coin.balance,
    balanceUsd = coin.balanceUsd,
    priceUsd = coin.price,
    reservedBalanceCoin = coin.reservedBalance,
    reservedBalanceUsd = coin.reservedBalanceUsd,
    publicKey = coin.address,
    code = coin.code,
    isEnabled = accountEntity.isEnabled,
    details = CoinDataItem.Details(
        coinDetails.txFee,
        coinDetails.byteFee,
        coinDetails.scale,
        coinDetails.platformSwapFee,
        coinDetails.platformTradeFee,
        coinDetails.walletAddress,
        coinDetails.gasLimit,
        coinDetails.gasPrice,
        coinDetails.convertedTxFee
    )
)