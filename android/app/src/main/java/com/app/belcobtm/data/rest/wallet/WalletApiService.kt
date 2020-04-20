package com.app.belcobtm.data.rest.wallet

import com.app.belcobtm.data.rest.wallet.request.CoinToCoinExchangeRequest
import com.app.belcobtm.data.rest.wallet.request.VerifySmsCodeRequest
import com.app.belcobtm.data.rest.wallet.request.WithdrawRequest
import com.app.belcobtm.data.rest.wallet.response.hash.BinanceBlockResponse
import com.app.belcobtm.data.rest.wallet.response.hash.TronRawDataResponse
import com.app.belcobtm.data.rest.wallet.response.hash.UtxoItemResponse
import com.app.belcobtm.data.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure

class WalletApiService(
    private val api: WalletApi,
    private val prefHelper: SharedPreferencesHelper
) {

    suspend fun withdraw(
        hash: String,
        coinFrom: String,
        coinFromAmount: Double
    ): Either<Failure, Unit> = try {
        val requestBody = WithdrawRequest(
            type = TRANSACTION_WITHDRAW,
            cryptoAmount = coinFromAmount,
            hex = hash
        )
        val request = api.withdrawAsync(
            prefHelper.userId,
            coinFrom,
            requestBody
        ).await()

        request.body()?.let { Either.Right(Unit) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun coinToCoinExchange(
        coinFromAmount: Double,
        coinFrom: String,
        coinTo: String,
        hash: String
    ): Either<Failure, Unit> = try {
        val requestBody = CoinToCoinExchangeRequest(
            type = TRANSACTION_SEND_COIN_TO_COIN,
            cryptoAmount = coinFromAmount,
            refCoin = coinTo,
            hex = hash
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

    suspend fun getUtxoList(coinId: String, publicKey: String): Either<Failure, List<UtxoItemResponse>> = try {
        val request = api.getUtxoListAsync(prefHelper.userId, coinId, publicKey).await()
        request.body()?.utxos?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun getEthereumNonce(): Either<Failure, Long?> = try {
        val request = api.getEthereumNonceAsync(prefHelper.userId).await()
        request.body()?.let { Either.Right(it.nonce) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun getRippleSequence(): Either<Failure, Long> = try {
        val request = api.getRippleBlockHeaderAsync(prefHelper.userId).await()
        request.body()?.let { Either.Right(it.sequence ?: 0) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun getBinanceBlockHeader(): Either<Failure, BinanceBlockResponse> = try {
        val request = api.getBinanceBlockHeaderAsync(prefHelper.userId).await()
        request.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun getTronBlockHeader(coinId: String): Either<Failure, TronRawDataResponse?> = try {
        val request = api.getTronBlockHeaderAsync(prefHelper.userId, coinId).await()
        request.body()?.let { Either.Right(it.blockHeader?.raw_data) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    companion object {
        const val TRANSACTION_WITHDRAW = 2
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