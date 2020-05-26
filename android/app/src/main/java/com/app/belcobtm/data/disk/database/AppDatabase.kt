package com.app.belcobtm.data.disk.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [CoinEntity::class], version = 1)
@TypeConverters(CoinTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getCoinDao(): CoinDao
}