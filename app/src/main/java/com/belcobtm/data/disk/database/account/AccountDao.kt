package com.belcobtm.data.disk.database.account

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.belcobtm.data.rest.wallet.response.BalanceResponse

@Dao
interface AccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: AccountEntity): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItemList(itemList: List<AccountEntity>)

    @Query("SELECT * FROM account_entity WHERE coin_name ==:code")
    suspend fun getAccountByName(code: String): AccountEntity

    @Query("SELECT * FROM account_entity")
    suspend fun getAccounts(): List<AccountEntity>?

    @Query("SELECT * FROM account_entity WHERE is_available = 1")
    suspend fun getAvailableAccounts(): List<AccountEntity>?

    @Query("DELETE FROM account_entity")
    suspend fun clearTable()

    @Query("SELECT * FROM account_entity LIMIT 1")
    suspend fun isTableHasItems(): AccountEntity?

    @Query("UPDATE account_entity SET is_available=:isAvailable WHERE coin_name = :name")
    suspend fun updateIsAvailableByName(name: String, isAvailable: Boolean)

    @Query("UPDATE account_entity SET is_enabled=:isEnabled WHERE coin_name = :name")
    suspend fun updateIsEnabledByName(name: String, isEnabled: Boolean)

    @Transaction
    suspend fun updateAccountEntityList(balance: BalanceResponse) {
        val accounts = getAccounts()
        val enabledCoins = balance.coins.map { it.coin }
        accounts?.forEach {
            updateIsAvailableByName(
                it.coinName,
                balance.availableCoins.contains(it.coinName)
            )
            updateIsEnabledByName(
                it.coinName,
                enabledCoins.contains(it.coinName)
            )
        }
    }

}
