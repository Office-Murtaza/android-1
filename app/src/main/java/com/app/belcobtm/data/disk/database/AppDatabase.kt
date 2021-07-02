package com.app.belcobtm.data.disk.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.app.belcobtm.data.disk.database.account.AccountDao
import com.app.belcobtm.data.disk.database.account.AccountEntity
import com.app.belcobtm.data.disk.database.account.CoinTypeConverter
import com.app.belcobtm.data.disk.database.wallet.CoinDetailsEntity
import com.app.belcobtm.data.disk.database.wallet.CoinEntity
import com.app.belcobtm.data.disk.database.wallet.WalletDao
import com.app.belcobtm.data.disk.database.wallet.WalletEntity

@Database(
    entities = [AccountEntity::class, CoinEntity::class, CoinDetailsEntity::class, WalletEntity::class],
    version = 3
)
@TypeConverters(CoinTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getCoinDao(): AccountDao
    abstract fun getWalletDao(): WalletDao

    companion object {

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                   CREATE TABLE IF NOT EXISTS coin (
                        coin_id INTEGER NOT NULL PRIMARY KEY,
                        idx INTEGER NOT NULL,
                        code TEXT NOT NULL,
                        address TEXT NOT NULL,
                        balance REAL NOT NULL,
                        balance_usd REAL NOT NULL,
                        reserved_balance REAL NOT NULL,
                        reserved_balance_usd REAL NOT NULL,
                        price REAL NOT NULL
                    ) 
                """
                )
                database.execSQL(
                    """
                   CREATE TABLE IF NOT EXISTS coin_detail (
                        c_id INTEGER NOT NULL PRIMARY KEY,
                        tx_fee DOUBLE NOT NULL,
                        byte_fee INTEGER NOT NULL,
                        scale INTEGER NOT NULL,
                        platform_swap_fee REAL NOT NULL,
                        platform_trade_fee REAL NOT NULL,
                        wallet_address TEXT NOT NULL,
                        gas_limit INTEGER,
                        gas_price INTEGER,
                        converted_tx_fee DOUBLE,
                        FOREIGN KEY(`c_id`) REFERENCES `coin`(`coin_id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    ) 
                """
                )
                database.execSQL(" CREATE TABLE IF NOT EXISTS wallet (total_balance REAL NOT NULL PRIMARY KEY)")
            }

        }
    }
}