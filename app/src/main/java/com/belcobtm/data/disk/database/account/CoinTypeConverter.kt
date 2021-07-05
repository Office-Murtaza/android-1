package com.belcobtm.data.disk.database.account

import androidx.room.TypeConverter
import com.belcobtm.domain.wallet.LocalCoinType

class CoinTypeConverter {
    @TypeConverter
    fun fromTypeName(name: String): LocalCoinType = LocalCoinType.valueOf(name)

    @TypeConverter
    fun toName(coinType: LocalCoinType): String = coinType.name
}