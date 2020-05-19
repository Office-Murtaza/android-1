package com.app.belcobtm.data.rest.wallet

import com.app.belcobtm.data.rest.wallet.request.*
import com.app.belcobtm.data.rest.wallet.response.hash.BinanceBlockResponse
import com.app.belcobtm.data.rest.wallet.response.hash.TronRawDataResponse
import com.app.belcobtm.data.rest.wallet.response.hash.UtxoItemResponse
import com.app.belcobtm.data.rest.wallet.response.mapToDataItem
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.item.SellLimitsDataItem
import com.app.belcobtm.domain.wallet.item.SellPreSubmitDataItem
import com.app.belcobtm.domain.wallet.item.TradeInfoDataItem

class WalletApiService(
    private val api: WalletApi,
    private val prefHelper: SharedPreferencesHelper
) {

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

    suspend fun getGiftAddress(
        coinFrom: String,
        phone: String
    ): Either<Failure, String> = try {
        val request = api.getGiftAddressAsync(prefHelper.userId, coinFrom, phone).await()
        request.body()?.let { Either.Right(it.address ?: "") } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun sendGift(
        hash: String,
        coinFrom: String,
        coinFromAmount: Double,
        giftId: String,
        phone: String,
        message: String
    ): Either<Failure, Unit> = try {
        val requestBody = SendGiftRequest(
            TRANSACTION_SEND_GIFT,
            coinFromAmount,
            phone,
            message,
            giftId,
            hash
        )
        val request = api.sendGiftAsync(prefHelper.userId, coinFrom, requestBody).await()
        request.body()?.let { Either.Right(Unit) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun sellGetLimitsAsync(
        coinFrom: String
    ): Either<Failure, SellLimitsDataItem> = try {
        val request = api.sellGetLimitsAsync(prefHelper.userId, coinFrom).await()
        request.body()?.let { Either.Right(it.mapToDataItem()) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun sellPreSubmit(
        coinFrom: String,
        coinFromAmount: Double,
        usdToAmount: Int
    ): Either<Failure, SellPreSubmitDataItem> = try {
        val requestBody = SellPreSubmitRequest(
            cryptoAmount = coinFromAmount,
            fiatAmount = usdToAmount,
            fiatCurrency = UNIT_USD
        )
        val request = api.sellPreSubmitAsync(
            prefHelper.userId,
            coinFrom,
            requestBody
        ).await()

        request.body()?.let { Either.Right(it.mapToDataItem()) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun sell(
        coinFromAmount: Double,
        coinFrom: String,
        hash: String
    ): Either<Failure, Unit> = try {
        val requestBody = SellRequest(
            type = TRANSACTION_SELL,
            cryptoAmount = coinFromAmount,
            hex = hash
        )
        val request = api.sellAsync(prefHelper.userId, coinFrom, requestBody).await()
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

    suspend fun getTradeInfo(): Either<Failure, TradeInfoDataItem> = try {
        val request = api.tradeGetInfoAsync(prefHelper.userId).await()
        request.body()?.let { Either.Right(it.mapToDataItem()) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun sendTradeUserLocation(latitude: Double, longitude: Double): Either<Failure, Unit> = try {
        val requestBody = TradeLocationRequest(latitude, longitude)
        val request = api.tradeSendUserLocationAsync(prefHelper.userId, requestBody).await()
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
        const val TRANSACTION_SEND_GIFT = 3
        const val TRANSACTION_SELL = 6
        const val TRANSACTION_SEND_COIN_TO_COIN = 8
        const val UNIT_USD = "USD"
    }
}