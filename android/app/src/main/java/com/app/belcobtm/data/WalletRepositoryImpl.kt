package com.app.belcobtm.data

import com.app.belcobtm.data.disk.database.AccountDao
import com.app.belcobtm.data.disk.database.mapToDataItem
import com.app.belcobtm.data.disk.database.mapToEntity
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.rest.wallet.WalletApiService
import com.app.belcobtm.data.rest.wallet.request.PriceChartPeriod
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.WalletRepository
import com.app.belcobtm.domain.wallet.item.*

class WalletRepositoryImpl(
    private val apiService: WalletApiService,
    private val prefHelper: SharedPreferencesHelper,
    private val daoAccount: AccountDao
) : WalletRepository {
    private val cachedCoinDataItemList: MutableList<CoinDataItem> = mutableListOf()

    override fun getCoinDetailsMap(): Map<String, CoinDetailsDataItem> = prefHelper.coinsDetails

    override fun getCoinDetailsItemByCode(
        coinCode: String
    ): CoinDetailsDataItem = prefHelper.coinsDetails.getValue(coinCode)

    override suspend fun getCoinItemByCode(
        coinCode: String
    ): Either<Failure, CoinDataItem> {
        val cachedCoinData = cachedCoinDataItemList.find { it.code == coinCode }
        return if (cachedCoinData != null)
            Either.Right(cachedCoinData)
        else
            getFreshCoinDataItem(coinCode)
    }

    override suspend fun getCoinItemList(): Either<Failure, List<CoinDataItem>> {
        return if (cachedCoinDataItemList.isNotEmpty())
            Either.Right(cachedCoinDataItemList)
        else
            getFreshCoinDataItems(daoAccount.getItemList()?.filter { it.isEnabled }?.map { it.type.name }.orEmpty())
    }

    override suspend fun getAccountList(): List<AccountDataItem> =
        (daoAccount.getItemList() ?: emptyList()).sortedBy { it.id }.map { it.mapToDataItem() }

    override suspend fun updateAccount(accountDataItem: AccountDataItem): Either<Failure, Unit> {
        val toggleCoinStateResult = apiService.toggleCoinState(
            accountDataItem.type.name,
            accountDataItem.isEnabled
        )
        return if (toggleCoinStateResult.isRight) {
            daoAccount.updateItem(accountDataItem.mapToEntity())
            Either.Right(Unit)
        } else {
            toggleCoinStateResult
        }
    }

    override suspend fun getFreshCoinDataItem(
        coinCode: String
    ): Either<Failure, CoinDataItem> {
        val enabledCodeList =
            daoAccount.getItemList()?.filter { it.isEnabled }?.map { it.type.name } ?: emptyList()
        val response = apiService.getBalance(enabledCodeList)
        return if (response.isRight) {
            val balanceItem = (response as Either.Right).b
            //TODO need find best way
            val enabledCoinList = balanceItem.coinList.map { coinItem ->
                coinItem.copy(isEnabled = enabledCodeList.firstOrNull { it == coinItem.code } != null)
            }
            cachedCoinDataItemList.clear()
            cachedCoinDataItemList.addAll(enabledCoinList)
            val coinDataItem = balanceItem.coinList.find { it.code == coinCode }
            if (coinDataItem == null) {
                Either.Left(Failure.MessageError("Data error"))
            } else {
                Either.Right(coinDataItem)
            }
        } else {
            response as Either.Left
        }
    }

    override suspend fun getFreshCoinDataItems(
        coinCodes: List<String>
    ): Either<Failure, List<CoinDataItem>> {
        val response = apiService.getBalance(coinCodes)
        return if (response is Either.Right) {
            cachedCoinDataItemList.clear()
            cachedCoinDataItemList.addAll(response.b.coinList)
            Either.Right(response.b.coinList)
        } else {
            response as Either.Left
        }
    }

    override suspend fun getBalanceItem(): Either<Failure, BalanceDataItem> {
        val enabledCodeList =
            daoAccount.getItemList()?.filter { it.isEnabled }?.map { it.type.name } ?: emptyList()
        val response = apiService.getBalance(enabledCodeList)
        if (response.isRight) {
            val balanceItem = (response as Either.Right).b
            val enabledCoinList = balanceItem.coinList.map { coinItem ->
                coinItem.copy(isEnabled = enabledCodeList.firstOrNull { it == coinItem.code } != null)
            }
            updateCoinsCache(enabledCoinList)
        }
        return response
    }

    override suspend fun getChart(
        coinCode: String,
        @PriceChartPeriod period: Int
    ): Either<Failure, ChartDataItem> = apiService.getChart(coinCode, period)

    override suspend fun updateCoinDetails(
        coinCode: String
    ): Either<Failure, CoinDetailsDataItem> {
        val response = apiService.getCoinDetails(coinCode)
        if (response.isRight) {
            val mutableCoinsFeeMap = prefHelper.coinsDetails.toMutableMap()
            mutableCoinsFeeMap[coinCode] = (response as Either.Right).b
            prefHelper.coinsDetails = mutableCoinsFeeMap
        }
        return response
    }

    override fun updateCoinsCache(coins: List<CoinDataItem>) {
        cachedCoinDataItemList.clear()
        cachedCoinDataItemList.addAll(coins)
    }
}
