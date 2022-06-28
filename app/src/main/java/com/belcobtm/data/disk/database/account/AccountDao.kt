package com.belcobtm.data.disk.database.account

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface AccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: AccountEntity): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItemList(itemList: List<AccountEntity>)

    @Update
    suspend fun updateItem(entity: AccountEntity)

    @Query("SELECT * FROM account_entity WHERE coin_name ==:code")
    suspend fun getItem(code: String): AccountEntity

    @Query("SELECT * FROM account_entity")
    suspend fun getItemList(): List<AccountEntity>?

    @Query("DELETE FROM account_entity")
    suspend fun clearTable()

    @Query("SELECT * FROM account_entity LIMIT 1")
    suspend fun isTableHasItems(): AccountEntity?
}
