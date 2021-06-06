package com.app.belcobtm.data.disk.database.wallet

import androidx.room.*
import com.app.belcobtm.data.rest.wallet.response.BalanceResponse
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
                    id, idx, code, address, balance,
                    fiatBalance, reservedBalance, reservedFiatBalance, price
                )
                coins.add(entity)
            }
            with(response.details) {
                val entity = CoinDetailsEntity(
                    response.id, txFee, byteFee, scale,
                    platformSwapFee, platformTradeFee, walletAddress,
                    gasLimit, gasPrice, convertedTxFee
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
        clearCoin()
        clearCoinDetails()
        clearWallet()
    }
}