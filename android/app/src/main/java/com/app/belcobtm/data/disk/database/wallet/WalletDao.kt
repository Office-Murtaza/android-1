package com.app.belcobtm.data.disk.database.wallet

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {

    @Query("SELECT * FROM coin_detail INNER JOIN coin INNER JOIN accountentity WHERE coin_detail.c_id = coin.coin_id AND coin.idx = accountentity.id")
    fun observeCoins(): Flow<List<FullCoinEntity>>

    @Query("SELECT * FROM wallet")
    fun observeWallet(): Flow<WalletEntity?>

    @Query("SELECT * FROM coin_detail INNER JOIN coin INNER JOIN accountentity WHERE coin_detail.c_id = coin.coin_id AND coin.idx = accountentity.id")
    suspend fun getCoins(): List<FullCoinEntity>

    @Query("SELECT * FROM coin_detail INNER JOIN coin INNER JOIN accountentity WHERE coin_detail.c_id = coin.coin_id AND coin.idx = accountentity.id AND coin.code = :code")
    suspend fun getCoinByCode(code: String): FullCoinEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateCoinDetails(coinDetails: List<CoinDetailsEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateCoins(coinDetails: List<CoinEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateWallet(walletEntity: WalletEntity)

    @Transaction
    suspend fun updateBalance(wallet: WalletEntity, coins: List<CoinEntity>, details: List<CoinDetailsEntity>) {
        updateWallet(wallet)
        updateCoins(coins)
        updateCoinDetails(details)
    }
}