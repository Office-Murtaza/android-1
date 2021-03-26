package com.app.belcobtm.data

import com.app.belcobtm.data.disk.database.AccountDao
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.rest.wallet.WalletApiService
import com.app.belcobtm.data.rest.wallet.request.PriceChartPeriod
import com.app.belcobtm.data.websockets.base.model.WalletBalance
import com.app.belcobtm.data.websockets.wallet.WalletObserver
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.WalletRepository
import com.app.belcobtm.domain.wallet.item.BalanceDataItem
import com.app.belcobtm.domain.wallet.item.ChartDataItem
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.domain.wallet.item.CoinDetailsDataItem
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow

class WalletRepositoryImpl(
    private val walletObserver: WalletObserver,
    private val apiService: WalletApiService,
    private val prefHelper: SharedPreferencesHelper,
    private val daoAccount: AccountDao
) : WalletRepository {

    override fun getCoinDetailsMap(): Map<String, CoinDetailsDataItem> = prefHelper.coinsDetails

    override fun getCoinDetailsItemByCode(
        coinCode: String
    ): CoinDetailsDataItem = prefHelper.coinsDetails.getValue(coinCode)

    override suspend fun getCoinItemByCode(
        coinCode: String
    ): Either<Failure, CoinDataItem> {
        val data = walletObserver.observe().receiveAsFlow()
            .filterIsInstance<WalletBalance.Balance>()
            .firstOrNull()
        if (data != null) {
            val coin = data.data.coinList.firstOrNull { it.code == coinCode }
            if (coin != null) return Either.Right(coin)
        }
        // fallback
        return getFreshCoinDataItem(coinCode)
    }

    override suspend fun getCoinItemList(): Either<Failure, List<CoinDataItem>> {
        val result = getBalanceItem()
        return if (result is Either.Right) {
            Either.Right(result.b.coinList)
        } else {
            val upstreamFailure = (result as Either.Left).a
            Either.Left(upstreamFailure)
        }
    }

    override suspend fun getFreshCoinDataItem(
        coinCode: String
    ): Either<Failure, CoinDataItem> {
        val enabledCodeList = daoAccount.getItemList()?.map { it.type.name } ?: emptyList()
        val response = apiService.getBalance(enabledCodeList)
        return if (response.isRight) {
            val balanceItem = (response as Either.Right).b
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
            Either.Right(response.b.coinList)
        } else {
            response as Either.Left
        }
    }

    override suspend fun getBalanceItem(): Either<Failure, BalanceDataItem> {
        val data = walletObserver.observe().receiveAsFlow()
            .filterIsInstance<WalletBalance.Balance>()
            .firstOrNull()
        if (data != null) {
            return Either.Right(data.data)
        }
        return Either.Left(Failure.ServerError())
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
}
