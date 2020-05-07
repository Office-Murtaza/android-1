package com.app.belcobtm.data.disk.database

import androidx.room.*

@Dao
interface CoinDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: CoinEntity): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItemList(itemList: List<CoinEntity>)

    @Update
    suspend fun updateItem(entity: CoinEntity)

    @Query("SELECT * FROM CoinEntity WHERE type ==:code")
    suspend fun getItem(code: String): CoinEntity

    @Query("SELECT * FROM CoinEntity")
    suspend fun getItemList(): List<CoinEntity>?

    @Query("DELETE FROM CoinEntity")
    suspend fun clearTable()
}