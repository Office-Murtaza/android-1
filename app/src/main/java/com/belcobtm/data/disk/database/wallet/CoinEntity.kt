package com.belcobtm.data.disk.database.wallet

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "coin")
data class CoinEntity(
    @ColumnInfo(name = "code") @PrimaryKey val code: String,
    @ColumnInfo(name = "address") val address: String,
    @ColumnInfo(name = "balance") val balance: Double,
    @ColumnInfo(name = "balance_usd") val balanceUsd: Double,
    @ColumnInfo(name = "reserved_balance") val reservedBalance: Double,
    @ColumnInfo(name = "reserved_balance_usd") val reservedBalanceUsd: Double,
    @ColumnInfo(name = "price") val price: Double
)
