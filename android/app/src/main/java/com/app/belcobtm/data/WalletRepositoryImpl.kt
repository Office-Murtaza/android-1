package com.app.belcobtm.data

import com.app.belcobtm.data.core.NetworkUtils
import com.app.belcobtm.data.disk.database.CoinDao
import com.app.belcobtm.data.disk.database.mapToDataItem
import com.app.belcobtm.data.disk.database.mapToEntity
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.rest.wallet.WalletApiService
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.WalletRepository
import com.app.belcobtm.domain.wallet.item.BalanceDataItem
import com.app.belcobtm.domain.wallet.item.ChartDataItem
import com.app.belcobtm.domain.wallet.item.CoinFeeDataItem
import com.app.belcobtm.domain.wallet.item.LocalCoinDataItem

class WalletRepositoryImpl(
    private val apiService: WalletApiService,
    private val prefHelper: SharedPreferencesHelper,
    private val networkUtils: NetworkUtils,
    private val daoCoin: CoinDao
) : WalletRepository {
    override fun getCoinFeeMap(): Map<String, CoinFeeDataItem> = prefHelper.coinsFee

    override suspend fun getLocalCoinList(): List<LocalCoinDataItem> =
        (daoCoin.getItemList() ?: emptyList()).map { it.mapToDataItem() }

    override suspend fun updateCoin(dataItemLocal: LocalCoinDataItem): Either<Failure, Unit> {
        daoCoin.updateItem(dataItemLocal.mapToEntity())
        return Either.Right(Unit)
    }

    override suspend fun getBalanceItem(): Either<Failure, BalanceDataItem> = if (networkUtils.isNetworkAvailable()) {
        val enabledCoinList = daoCoin.getItemList()?.filter { it.isEnabled }?.map { it.type.name } ?: emptyList()
        apiService.getBalance(enabledCoinList)
    } else {
        Either.Left(Failure.NetworkConnection)
    }

    override suspend fun getChart(
        coinCode: String
    ): Either<Failure, ChartDataItem> = if (networkUtils.isNetworkAvailable()) {
        apiService.getChart(coinCode)
    } else {
        Either.Left(Failure.NetworkConnection)
    }

    override suspend fun updateCoinFee(
        coinCode: String
    ): Either<Failure, CoinFeeDataItem> = if (networkUtils.isNetworkAvailable()) {
        val response = apiService.getCoinFee(coinCode)
        val mutableCoinsFeeMap = prefHelper.coinsFee.toMutableMap()
        mutableCoinsFeeMap[coinCode] = (response as Either.Right).b
        prefHelper.coinsFee = mutableCoinsFeeMap
        response
    } else {
        Either.Left(Failure.NetworkConnection)
    }
}