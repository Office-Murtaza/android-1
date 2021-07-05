package com.belcobtm.data.rest.transaction

import com.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.belcobtm.data.rest.transaction.request.*
import com.belcobtm.data.rest.transaction.response.GetTransactionsResponse
import com.belcobtm.data.rest.transaction.response.TransactionDetailsResponse
import com.belcobtm.data.rest.transaction.response.hash.BinanceBlockResponse
import com.belcobtm.data.rest.transaction.response.hash.TronRawDataResponse
import com.belcobtm.data.rest.transaction.response.hash.UtxoItemResponse
import com.belcobtm.data.rest.transaction.response.mapToDataItem
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.transaction.item.SellLimitsDataItem
import com.belcobtm.domain.transaction.item.SellPreSubmitDataItem
import com.belcobtm.domain.transaction.item.StakeDetailsDataItem

class TransactionApiService(
    private val api: TransactionApi,
    private val prefHelper: SharedPreferencesHelper
) {

    suspend fun fetchTransactions(coinCode: String): Either<Failure, GetTransactionsResponse> =
        try {
            val request = api.getTransactionsAsync(prefHelper.userId, coinCode).await()
            request.body()?.let { body -> Either.Right(body) } ?: Either.Left(Failure.ServerError())
        } catch (failure: Failure) {
            failure.printStackTrace()
            Either.Left(failure)
        }

    suspend fun withdraw(
        hash: String,
        coinFrom: String,
        coinFromAmount: Double,
        fee: Double?,
        fromAddress: String?,
        toAddress: String?
    ): Either<Failure, TransactionDetailsResponse> = try {
        val requestBody = WithdrawRequest(
            type = TRANSACTION_WITHDRAW,
            cryptoAmount = coinFromAmount,
            hex = hash,
            fee = fee,
            fromAddress = fromAddress,
            toAddress = toAddress
        )
        val request = api.withdrawAsync(prefHelper.userId, coinFrom, requestBody).await()
        request.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun getGiftAddress(
        coinFrom: String,
        phone: String
    ): Either<Failure, String> = try {
        val request = api.getGiftAddressAsync(coinFrom, phone).await()
        request.body()?.let { Either.Right(it.address ?: "") } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun sendGift(
        hash: String,
        coinFrom: String,
        coinFromAmount: Double,
        giftId: String?,
        phone: String,
        message: String?,
        fee: Double? = null,
        fromAddress: String? = null,
        toAddress: String? = null
    ): Either<Failure, TransactionDetailsResponse> = try {
        val requestBody = SendGiftRequest(
            TRANSACTION_SEND_GIFT,
            coinFromAmount,
            phone,
            message,
            giftId,
            hash,
            fee,
            fromAddress,
            toAddress
        )
        val request = api.sendGiftAsync(prefHelper.userId, coinFrom, requestBody).await()
        request.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun sellGetLimitsAsync(): Either<Failure, SellLimitsDataItem> = try {
        val request = api.sellGetLimitsAsync(prefHelper.userId).await()
        request.body()?.let { Either.Right(it.mapToDataItem()) }
            ?: Either.Left(Failure.ServerError())
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

        request.body()?.let { Either.Right(it.mapToDataItem()) }
            ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun sell(
        coinFromAmount: Double,
        coinFrom: String,
        hash: String
    ): Either<Failure, TransactionDetailsResponse> = try {
        val requestBody = SellRequest(
            type = TRANSACTION_SELL,
            cryptoAmount = coinFromAmount,
            hex = hash
        )
        val request = api.sellAsync(prefHelper.userId, coinFrom, requestBody).await()
        request.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun exchange(
        coinFromAmount: Double,
        coinToAmount: Double,
        coinFrom: String,
        coinTo: String,
        hash: String,
        fee: Double?,
        fromAddress: String?,
        toAddress: String?
    ): Either<Failure, TransactionDetailsResponse> = try {
        val requestBody = CoinToCoinExchangeRequest(
            type = TRANSACTION_SEND_COIN_TO_COIN,
            hex = hash,
            fromAddress = fromAddress,
            toAddress = toAddress,
            cryptoAmount = coinFromAmount,
            refCoin = coinTo,
            refCryptoAmount = coinToAmount,
            fee = fee,
        )
        val request = api.exchangeAsync(prefHelper.userId, coinFrom, requestBody).await()
        request.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun getUtxoList(
        coinId: String,
        publicKey: String
    ): Either<Failure, List<UtxoItemResponse>> = try {
        val request = api.getUtxoListAsync(coinId, publicKey).await()
        request.body()?.utxos?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun getEthereumNonce(toAddress: String): Either<Failure, Long?> =
        try {
            val request = api.getEthereumNonceAsync(toAddress).await()
            request.body()?.let { Either.Right(it.nonce) } ?: Either.Left(Failure.ServerError())
        } catch (failure: Failure) {
            failure.printStackTrace()
            Either.Left(failure)
        }

    suspend fun getRippleSequence(fromAddress: String): Either<Failure, Long> = try {
        val request = api.getRippleBlockHeaderAsync(fromAddress).await()
        request.body()?.let { Either.Right(it.sequence ?: 0) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun checkRippleAccountActivation(fromAddress: String): Either<Failure, Boolean> = try {
        val request = api.checkRippleAccountActivationAsync(fromAddress).await()
        request.body()?.let { Either.Right(it.result ?: false) }
            ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun getBinanceBlockHeader(toAddress: String): Either<Failure, BinanceBlockResponse> =
        try {
            val request = api.getBinanceBlockHeaderAsync(toAddress).await()
            request.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
        } catch (failure: Failure) {
            failure.printStackTrace()
            Either.Left(failure)
        }

    suspend fun getTronBlockHeader(): Either<Failure, TronRawDataResponse?> = try {
        val request = api.getTronBlockHeaderAsync().await()
        request.body()?.let { Either.Right(it.blockHeader?.raw_data) }
            ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun submitRecall(
        coinCode: String,
        cryptoAmount: Double
    ): Either<Failure, TransactionDetailsResponse> =
        try {
            val requestBody = TradeRecallRequest(TRANSACTION_TRADE_RECALL, cryptoAmount)
            val request = api.submitRecallAsync(prefHelper.userId, coinCode, requestBody).await()
            request.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
        } catch (failure: Failure) {
            failure.printStackTrace()
            Either.Left(failure)
        }

    suspend fun submitReserve(
        coinCode: String, fromAddress: String,
        toAddress: String,
        cryptoAmount: Double,
        fee: Double,
        hex: String
    ): Either<Failure, TransactionDetailsResponse> = try {
        val requestBody = TradeReserveRequest(
            TRANSACTION_TRADE_RESERVE,
            fromAddress,
            toAddress,
            cryptoAmount,
            fee,
            hex
        )
        val request = api.submitReserveAsync(prefHelper.userId, coinCode, requestBody).await()
        request.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun stakeDetails(coinCode: String): Either<Failure, StakeDetailsDataItem> = try {
        val request = api.stakeDetailsAsync(prefHelper.userId, coinCode).await()
        request.body()?.let { Either.Right(it.mapToDataItem()) }
            ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun stakeCreate(
        coinCode: String,
        fromAddress: String,
        toAddress: String,
        cryptoAmount: Double,
        fee: Double,
        hex: String
    ): Either<Failure, TransactionDetailsResponse> = try {
        val requestBody =
            StakeRequest(TRANSACTION_STAKE, fromAddress, toAddress, cryptoAmount, fee, hex)
        val request = api.stakeOrUnStakeAsync(prefHelper.userId, coinCode, requestBody).await()
        request.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun stakeCancel(
        coinCode: String,
        fromAddress: String,
        toAddress: String,
        cryptoAmount: Double,
        fee: Double,
        hex: String
    ): Either<Failure, TransactionDetailsResponse> = try {
        val requestBody =
            StakeRequest(TRANSACTION_STAKE_CANCEL, fromAddress, toAddress, cryptoAmount, fee, hex)
        val request = api.stakeOrUnStakeAsync(prefHelper.userId, coinCode, requestBody).await()
        request.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun unStake(
        coinCode: String,
        fromAddress: String,
        toAddress: String,
        cryptoAmount: Double,
        fee: Double,
        hex: String
    ): Either<Failure, TransactionDetailsResponse> = try {
        val requestBody = StakeRequest(
            TRANSACTION_WITHTRADW_STAKE,
            fromAddress,
            toAddress,
            cryptoAmount,
            fee,
            hex
        )
        val request = api.stakeOrUnStakeAsync(prefHelper.userId, coinCode, requestBody).await()
        request.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun getXRPAddressActivated(address: String): Either<Failure, Boolean> = try {
        val request = api.checkRippleAccountActivationAsync(address).await()
        request.body()?.let { Either.Right(it.result ?: false) }
            ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    companion object {
        const val TRANSACTION_WITHDRAW = 2
        const val TRANSACTION_SEND_GIFT = 3
        const val TRANSACTION_SELL = 6
        const val TRANSACTION_SEND_COIN_TO_COIN = 8
        const val TRANSACTION_STAKE = 13
        const val TRANSACTION_STAKE_CANCEL = 14
        const val TRANSACTION_WITHTRADW_STAKE = 15

        const val TRANSACTION_TRADE_RECALL = 11
        const val TRANSACTION_TRADE_RESERVE = 10
        const val UNIT_USD = "USD"
    }
}