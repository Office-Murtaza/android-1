package com.belcobtm.data.disk.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.belcobtm.data.disk.database.account.AccountDao
import com.belcobtm.data.disk.database.account.AccountEntity
import com.belcobtm.data.disk.database.account.CoinTypeConverter
import com.belcobtm.data.disk.database.service.ServiceDao
import com.belcobtm.data.disk.database.service.ServiceEntity
import com.belcobtm.data.disk.database.wallet.CoinDetailsEntity
import com.belcobtm.data.disk.database.wallet.CoinEntity
import com.belcobtm.data.disk.database.wallet.WalletDao
import com.belcobtm.data.disk.database.wallet.WalletEntity

@Database(
    entities = [
        AccountEntity::class,
        CoinEntity::class,
        CoinDetailsEntity::class,
        WalletEntity::class,
        ServiceEntity::class
    ],
    version = 7
)
@TypeConverters(CoinTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getCoinDao(): AccountDao
    abstract fun getWalletDao(): WalletDao
    abstract fun getServiceDao(): ServiceDao

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
                database.execSQL("CREATE TABLE IF NOT EXISTS wallet (total_balance REAL NOT NULL PRIMARY KEY)")
            }
        }
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                   CREATE TABLE IF NOT EXISTS service (
                        id INTEGER NOT NULL PRIMARY KEY,
                        fee_percent REAL NOT NULL
                    ) 
                """
                )
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS coin_detail_copy (
                        c_id INTEGER NOT NULL PRIMARY KEY,
                        tx_fee DOUBLE NOT NULL,
                        byte_fee INTEGER NOT NULL,
                        scale INTEGER NOT NULL,
                        wallet_address TEXT NOT NULL,
                        gas_limit INTEGER,
                        gas_price INTEGER,
                        converted_tx_fee DOUBLE,
                        FOREIGN KEY(`c_id`) REFERENCES `coin`(`coin_id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    ) 
                """
                )
                database.execSQL(
                    """
                    INSERT INTO coin_detail_copy 
                    SELECT c_id, tx_fee, byte_fee, scale, wallet_address, 
                           gas_limit, gas_price, converted_tx_fee 
                    FROM coin_detail
                """
                )
                database.execSQL("DROP TABLE coin_detail")
                database.execSQL("ALTER TABLE coin_detail_copy RENAME to coin_detail")
            }
        }
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE coin")
                database.execSQL("DROP TABLE coin_detail")
                database.execSQL("DROP TABLE AccountEntity")
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS coin_detail (
                        c_code TEXT NOT NULL PRIMARY KEY,
                        tx_fee DOUBLE NOT NULL,
                        byte_fee INTEGER NOT NULL,
                        scale INTEGER NOT NULL,
                        wallet_address TEXT NOT NULL,
                        gas_limit INTEGER,
                        gas_price INTEGER,
                        converted_tx_fee REAL,
                        FOREIGN KEY(`c_code`) REFERENCES `coin`(`code`) ON UPDATE NO ACTION ON DELETE CASCADE
                    ) 
                """
                )
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS coin (
                        code TEXT NOT NULL PRIMARY KEY,
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
                    CREATE TABLE IF NOT EXISTS account_entity (
                        coin_name TEXT NOT NULL PRIMARY KEY,
                        public_key TEXT NOT NULL,
                        private_key TEXT NOT NULL,
                        is_enabled INTEGER NOT NULL,
                        FOREIGN KEY(`coin_name`) REFERENCES `coin`(`code`) ON UPDATE NO ACTION ON DELETE CASCADE
                    ) 
                """
                )
            }
        }
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE coin_detail")
                database.execSQL("DROP TABLE account_entity")
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS coin_detail (
                        c_code TEXT NOT NULL PRIMARY KEY,
                        wallet_address TEXT NOT NULL,
                        FOREIGN KEY(`c_code`) REFERENCES `coin`(`code`) ON UPDATE NO ACTION ON DELETE CASCADE
                    ) 
                """
                )
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS account_entity (
                        coin_name TEXT NOT NULL PRIMARY KEY,
                        public_key TEXT NOT NULL,
                        private_key TEXT NOT NULL,
                        is_enabled INTEGER NOT NULL
                    ) 
                """
                )
            }
        }
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """ALTER TABLE coin_detail ADD COLUMN coin_index INTEGER default 0
                """
                )
            }
        }
    }
}