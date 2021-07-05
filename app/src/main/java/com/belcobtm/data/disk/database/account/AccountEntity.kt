package com.belcobtm.data.disk.database.account

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.domain.wallet.item.AccountDataItem

@Entity
data class AccountEntity(
    @PrimaryKey val id: Int,
    val type: LocalCoinType,
    val publicKey: String,
    val privateKey: String,
    val isEnabled: Boolean = true
)

fun AccountEntity.mapToDataItem(): AccountDataItem = AccountDataItem(
    id = id,
    type = type,
    publicKey = publicKey,
    privateKey = privateKey
).also { it.isEnabled = isEnabled }

fun AccountDataItem.mapToEntity(): AccountEntity =
    AccountEntity(
        id = id,
        type = type,
        publicKey = publicKey,
        privateKey = privateKey,
        isEnabled = isEnabled
    )