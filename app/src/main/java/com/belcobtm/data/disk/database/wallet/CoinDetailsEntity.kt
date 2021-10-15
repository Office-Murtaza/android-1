package com.belcobtm.data.disk.database.wallet

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "coin_detail",
    foreignKeys = [
        ForeignKey(
            entity = CoinEntity::class,
            parentColumns = ["code"],
            childColumns = ["c_code"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CoinDetailsEntity(
    @ColumnInfo(name = "c_code") @PrimaryKey val id: String,
    @ColumnInfo(name = "coin_index") val index: Int,
    @ColumnInfo(name = "wallet_address") val walletAddress: String,
    @ColumnInfo(name = "contract_address") val contractAddress: String,
)