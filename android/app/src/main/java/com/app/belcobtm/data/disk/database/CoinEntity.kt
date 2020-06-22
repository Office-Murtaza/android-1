package com.app.belcobtm.data.disk.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.domain.wallet.item.LocalCoinDataItem

@Entity
data class CoinEntity(
    @PrimaryKey val type: LocalCoinType,
    val publicKey: String,
    val privateKey: String,
    val isEnabled: Boolean = true
)

fun CoinEntity.mapToDataItem(): LocalCoinDataItem = LocalCoinDataItem(
    type = type,
    publicKey = publicKey,
    privateKey = privateKey
).also { it.isEnabled = isEnabled }

fun LocalCoinDataItem.mapToEntity(): CoinEntity = CoinEntity(
    type = type,
    publicKey = publicKey,
    privateKey = privateKey,
    isEnabled = isEnabled
)