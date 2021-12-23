package com.belcobtm.data.disk.database.service

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "service")
data class ServiceEntity(
    @ServiceType @PrimaryKey val id: Int,
    @ColumnInfo(name = "fee_percent") val feePercent: Double,
    @ColumnInfo(name = "tx_limit") val txLimit: Double,
    @ColumnInfo(name = "daily_limit") val dailyLimit: Double,
    @ColumnInfo(name = "remain_limit") val remainLimit: Double,
)