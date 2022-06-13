package com.belcobtm.data.rest.wallet

import com.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.belcobtm.data.rest.wallet.request.PriceChartPeriod
import com.belcobtm.data.rest.wallet.response.mapToDataItem
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.wallet.item.BalanceDataItem
import com.belcobtm.domain.wallet.item.ChartDataItem
import java.net.HttpURLConnection

class WalletApiService(
    private val api: WalletApi,
    private val prefHelper: SharedPreferencesHelper
) {

    suspend fun getBalance(enabledCoinList: List<String>): Either<Failure, BalanceDataItem> = try {
        val request = api.getBalanceAsync(prefHelper.userId, enabledCoinList)
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
        val request = api.getChartAsync(coinCode, period)
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
        val request = api.toggleCoinStateAsync(prefHelper.userId, coinCode, enabled)
        request.takeIf { it.code() == HttpURLConnection.HTTP_OK }
            ?.let { Either.Right(Unit) }
            ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

}
