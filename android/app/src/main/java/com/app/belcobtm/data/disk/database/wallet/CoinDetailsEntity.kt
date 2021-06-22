package com.app.belcobtm.data.disk.database.wallet

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "coin_detail",
    foreignKeys = [
        ForeignKey(
            entity = CoinEntity::class,
            parentColumns = ["coin_id"],
            childColumns = ["c_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CoinDetailsEntity(
    @ColumnInfo(name = "c_id") @PrimaryKey val id: Int,
    @ColumnInfo(name = "tx_fee") val txFee: Double,
    @ColumnInfo(name = "byte_fee") val byteFee: Long,
    @ColumnInfo(name = "scale") val scale: Int,
    @ColumnInfo(name = "platform_swap_fee") val platformSwapFee: Double,
    @ColumnInfo(name = "platform_trade_fee") val platformTradeFee: Double,
    @ColumnInfo(name = "wallet_address") val walletAddress: String,
    @ColumnInfo(name = "gas_limit") val gasLimit: Long?,
    @ColumnInfo(name = "gas_price") val gasPrice: Long?,
    @ColumnInfo(name = "converted_tx_fee") val convertedTxFee: Double?
)