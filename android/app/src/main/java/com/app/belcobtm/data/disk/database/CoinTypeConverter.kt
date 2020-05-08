package com.app.belcobtm.data.disk.database

import androidx.room.TypeConverter
import com.app.belcobtm.domain.wallet.LocalCoinType
import wallet.core.jni.CoinType

class CoinTypeConverter {
    @TypeConverter
    fun fromTypeName(name: String): LocalCoinType = LocalCoinType.valueOf(name)

    @TypeConverter
    fun toName(coinType: LocalCoinType): String = coinType.name
}