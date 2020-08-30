package com.app.belcobtm.data.rest.wallet

import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.rest.wallet.response.mapToDataItem
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.item.BalanceDataItem
import com.app.belcobtm.domain.wallet.item.ChartDataItem
import com.app.belcobtm.domain.wallet.item.CoinFeeDataItem

class WalletApiService(
    private val api: WalletApi,
    private val prefHelper: SharedPreferencesHelper
) {

    suspend fun getBalance(enabledCoinList: List<String>): Either<Failure, BalanceDataItem> = try {
        val request = api.getBalanceAsync(prefHelper.userId, enabledCoinList).await()
        request.body()?.let {
            Either.Right(it.mapToDataItem())
        } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun getChart(coinCode: String): Either<Failure, ChartDataItem> = try {
        val request = api.getChartAsync(prefHelper.userId, coinCode).await()
        request.body()?.let { Either.Right(it.mapToDataItem()) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun getCoinFee(coinCode: String): Either<Failure, CoinFeeDataItem> = try {
        val request = api.getCoinFeeAsync(coinCode).await()
        request.body()?.let { Either.Right(it.mapToDataItem()) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }
}