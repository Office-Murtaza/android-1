package com.app.belcobtm.data.disk.database

import androidx.room.*

@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: AccountEntity): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItemList(itemList: List<AccountEntity>)

    @Update
    suspend fun updateItem(entity: AccountEntity)

    @Query("SELECT * FROM AccountEntity WHERE type ==:code")
    suspend fun getItem(code: String): AccountEntity

    @Query("SELECT * FROM AccountEntity")
    suspend fun getItemList(): List<AccountEntity>?

    @Query("DELETE FROM AccountEntity")
    suspend fun clearTable()

    @Query("SELECT * FROM AccountEntity LIMIT 1")
    suspend fun isTableHasItems(): AccountEntity?
}