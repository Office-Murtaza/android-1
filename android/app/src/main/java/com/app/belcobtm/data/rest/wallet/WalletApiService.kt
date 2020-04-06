package com.app.belcobtm.data.rest.wallet

import com.app.belcobtm.data.rest.wallet.request.CoinToCoinExchangeRequest
import com.app.belcobtm.data.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure

class WalletApiService(
    private val api: WalletApi,
    private val prefHelper: SharedPreferencesHelper
) {
    suspend fun coinToCoinExchange(
        coinFromAmount: Double,
        coinFrom: String,
        coinTo: String,
        hex: String
    ): Either<Failure, Unit>  = try {
        val requestBody = CoinToCoinExchangeRequest(
            amount = coinFromAmount,
            coinCode = coinFrom,
            refCoinCode = coinTo,
            hex = hex
        )
        val request = api.coinToCoinExchangeAsync(
            prefHelper.userId,
            coinFrom,
            requestBody
        ).await()

        request.body()?.let { Either.Right(Unit) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }
}