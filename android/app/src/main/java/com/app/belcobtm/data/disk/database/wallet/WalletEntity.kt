package com.app.belcobtm.data.disk.database.wallet

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallet")
data class WalletEntity(
    @ColumnInfo(name = "total_balance") @PrimaryKey val totalBalance: Double
)