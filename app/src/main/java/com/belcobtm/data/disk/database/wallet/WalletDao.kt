package com.belcobtm.data.disk.database.wallet

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.belcobtm.data.rest.wallet.response.BalanceResponse
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {

    @Query(
        """
        SELECT * 
        FROM coin_detail INNER JOIN coin INNER JOIN account_entity 
        WHERE coin_detail.c_code = coin.code AND coin.code = account_entity.coin_name
        ORDER BY coin_detail.coin_index
    """
    )
    fun observeCoins(): Flow<List<FullCoinEntity>>

    @Query("SELECT * FROM wallet")
    fun observeWallet(): Flow<WalletEntity?>

    @Query(
        """
        SELECT *
        FROM coin_detail INNER JOIN coin INNER JOIN account_entity
        WHERE coin_detail.c_code = coin.code AND coin.code = account_entity.coin_name
        ORDER BY coin_detail.coin_index
    """
    )
    suspend fun getCoins(): List<FullCoinEntity>

    @Query(
        """
        SELECT * 
        FROM account_entity INNER JOIN  coin_detail INNER JOIN coin
        WHERE account_entity.coin_name = coin.code AND coin_detail.c_code = coin.code  AND account_entity.coin_name = :code
    """
    )
    suspend fun getCoinByCode(code: String): FullCoinEntity

    @Query("SELECT total_balance FROM wallet")
    suspend fun getTotalBalance(): Double

    @Query("DELETE FROM wallet")
    suspend fun clearBalance()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateCoinDetails(coinDetails: List<CoinDetailsEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateCoins(coinDetails: List<CoinEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateWallet(walletEntity: WalletEntity)

    @Query("DELETE FROM coin")
    suspend fun clearCoin()

    @Query("DELETE FROM coin_detail")
    suspend fun clearCoinDetails()

    @Query("DELETE FROM wallet")
    suspend fun clearWallet()

    @Transaction
    suspend fun updateBalance(balanceResponse: BalanceResponse) {
        val wallet = WalletEntity(balanceResponse.totalBalance)
        val coins = ArrayList<CoinEntity>()
        val details = ArrayList<CoinDetailsEntity>()
        balanceResponse.coins.forEach { response ->
            with(response) {
                val entity = CoinEntity(
                    coin, address, balance, fiatBalance,
                    reserved, fiatReserved, price
                )
                coins.add(entity)
            }
            with(response.details) {
                val entity = CoinDetailsEntity(
                    response.coin, response.details.index,
                    serverAddress, contractAddress.orEmpty()
                )
                details.add(entity)
            }
        }
        updateWallet(wallet)
        updateCoins(coins)
        updateCoinDetails(details)
    }

    @Transaction
    suspend fun clear() {
        clearCoinDetails()
        clearCoin()
        clearWallet()
    }

}
