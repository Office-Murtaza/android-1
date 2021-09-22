package com.belcobtm.data.disk.database.account

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.belcobtm.data.disk.database.wallet.CoinEntity
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.domain.wallet.item.AccountDataItem

@Entity(tableName = "account_entity")
data class AccountEntity(
    @ColumnInfo(name = "coin_name") @PrimaryKey val coinName: String,
    @ColumnInfo(name = "public_key") val publicKey: String,
    @ColumnInfo(name = "private_key") val privateKey: String,
    @ColumnInfo(name = "is_enabled") val isEnabled: Boolean = true
) {

    val type: LocalCoinType
        get() = LocalCoinType.valueOf(coinName)

}

fun AccountEntity.mapToDataItem(): AccountDataItem = AccountDataItem(
    type = type,
    publicKey = publicKey,
    privateKey = privateKey
).also { it.isEnabled = isEnabled }

fun AccountDataItem.mapToEntity(): AccountEntity =
    AccountEntity(
        coinName = type.name,
        publicKey = publicKey,
        privateKey = privateKey,
        isEnabled = isEnabled
    )