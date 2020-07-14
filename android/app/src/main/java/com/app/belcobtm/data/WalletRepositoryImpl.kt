package com.app.belcobtm.data

import com.app.belcobtm.data.core.NetworkUtils
import com.app.belcobtm.data.disk.database.AccountDao
import com.app.belcobtm.data.disk.database.mapToDataItem
import com.app.belcobtm.data.disk.database.mapToEntity
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.rest.wallet.WalletApiService
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.WalletRepository
import com.app.belcobtm.domain.wallet.item.*

class WalletRepositoryImpl(
    private val apiService: WalletApiService,
    private val prefHelper: SharedPreferencesHelper,
    private val networkUtils: NetworkUtils,
    private val daoAccount: AccountDao
) : WalletRepository {
    private val cachedCoinDataItemList: MutableList<CoinDataItem> = mutableListOf()

    override fun getCoinFeeMap(): Map<String, CoinFeeDataItem> = prefHelper.coinsFee

    override fun getCoinFeeItemByCode(coinCode: String): CoinFeeDataItem = prefHelper.coinsFee[coinCode] ?: error("")

    override fun getCoinItemByCode(
        coinCode: String
    ): CoinDataItem = cachedCoinDataItemList.find { it.code == coinCode }!!

    override suspend fun getAccountList(): List<AccountDataItem> =
        (daoAccount.getItemList() ?: emptyList()).map { it.mapToDataItem() }

    override suspend fun updateAccount(accountDataItem: AccountDataItem): Either<Failure, Unit> {
        daoAccount.updateItem(accountDataItem.mapToEntity())
        return Either.Right(Unit)
    }

    override suspend fun getFreshCoinDataItem(
        coinCode: String
    ): Either<Failure, CoinDataItem> = if (networkUtils.isNetworkAvailable()) {
        val enabledCoinList = daoAccount.getItemList()?.filter { it.isEnabled }?.map { it.type.name } ?: emptyList()
        val response = apiService.getBalance(enabledCoinList)
        if (response.isRight) {
            val balanceItem = (response as Either.Right).b
            cachedCoinDataItemList.clear()
            cachedCoinDataItemList.addAll(balanceItem.coinList)
            val coinDataItem = balanceItem.coinList.find { it.code == coinCode }
            if (coinDataItem == null) {
                Either.Left(Failure.MessageError("Data error"))
            } else {
                Either.Right(coinDataItem)
            }
        } else {
            response as Either.Left
        }
    } else {
        Either.Left(Failure.NetworkConnection)
    }

    override suspend fun getBalanceItem(): Either<Failure, BalanceDataItem> = if (networkUtils.isNetworkAvailable()) {
        val enabledCoinList = daoAccount.getItemList()?.filter { it.isEnabled }?.map { it.type.name } ?: emptyList()
        val response = apiService.getBalance(enabledCoinList)
        if (response.isRight) {
            val balanceItem = (response as Either.Right).b
            cachedCoinDataItemList.clear()
            cachedCoinDataItemList.addAll(balanceItem.coinList)
        }
        response
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