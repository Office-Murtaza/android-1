package com.app.belcobtm.data.rest.wallet

import com.app.belcobtm.data.rest.wallet.request.CoinToCoinExchangeRequest
import com.app.belcobtm.data.rest.wallet.request.VerifySmsCodeRequest
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
    ): Either<Failure, Unit> = try {
        val requestBody = CoinToCoinExchangeRequest(
            type = TRANSACTION_SEND_COIN_TO_COIN,
            cryptoAmount = coinFromAmount,
            refCoin = coinTo,
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

    suspend fun sendToDeviceSmsCode(): Either<Failure, Unit> = try {
        val request = api.sendSmsCodeAsync(prefHelper.userId).await()
        request.body()?.let {
            if (request.isSuccessful) {
                Either.Right(Unit)
            } else {
                Either.Left(Failure.ServerError())
            }
        } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun verifySmsCode(smsCode: String): Either<Failure, Unit> = try {
        val request = api.verifySmsCodeAsync(prefHelper.userId, VerifySmsCodeRequest(smsCode)).await()
        request.body()?.let { Either.Right(Unit) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    companion object {
        const val TRANSACTION_SEND_COIN_TO_COIN = 8
//    case .unknown: return 0
//    case .deposit: return 1
//    case .withdraw: return 2
//    case .sendGift: return 3
//    case .receiveGift: return 4
//    case .buy: return 5
//    case .sell: return 6
//    case .sendC2C: return 8
//    case .receiveC2C: return 9
    }
}