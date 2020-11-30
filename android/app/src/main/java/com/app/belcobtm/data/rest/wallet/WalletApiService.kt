package com.app.belcobtm.data.rest.wallet

import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.rest.wallet.request.PriceChartPeriod
import com.app.belcobtm.data.rest.wallet.response.mapToDataItem
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.item.BalanceDataItem
import com.app.belcobtm.domain.wallet.item.ChartDataItem
import com.app.belcobtm.domain.wallet.item.CoinDetailsDataItem
import java.net.HttpURLConnection

class WalletApiService(
    private val api: WalletApi,
    private val prefHelper: SharedPreferencesHelper
) {

    suspend fun getBalance(enabledCoinList: List<String>): Either<Failure, BalanceDataItem> = try {
        val request = api.getBalanceAsync(prefHelper.userId, enabledCoinList).await()
        request.body()?.let { Either.Right(it.mapToDataItem()) }
            ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun getChart(
        coinCode: String,
        @PriceChartPeriod period: Int
    ): Either<Failure, ChartDataItem> = try {
        val request = api.getChartAsync(coinCode, period).await()
        request.body()?.let { Either.Right(it.mapToDataItem()) }
            ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun getCoinDetails(coinCode: String): Either<Failure, CoinDetailsDataItem> = try {
        val request = api.getCoinDetailsAsync(coinCode).await()
        request.body()?.let { Either.Right(it.mapToDataItem()) }
            ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun toggleCoinState(
        coinCode: String,
        enabled: Boolean
    ): Either<Failure, Unit> = try {
        val request = api.toggleCoinStateAsync(prefHelper.userId, coinCode, enabled).await()
        request.takeIf { it.code() == HttpURLConnection.HTTP_OK }
            ?.let { Either.Right(Unit) }
            ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }
}