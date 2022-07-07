package com.belcobtm.data.rest.transaction

import android.location.Location
import com.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.belcobtm.data.provider.location.LocationProvider
import com.belcobtm.data.rest.transaction.request.CoinToCoinExchangeRequest
import com.belcobtm.data.rest.transaction.request.SellPreSubmitRequest
import com.belcobtm.data.rest.transaction.request.SellRequest
import com.belcobtm.data.rest.transaction.request.SendGiftRequest
import com.belcobtm.data.rest.transaction.request.StakeRequest
import com.belcobtm.data.rest.transaction.request.TradeRecallRequest
import com.belcobtm.data.rest.transaction.request.TradeReserveRequest
import com.belcobtm.data.rest.transaction.request.WithdrawRequest
import com.belcobtm.data.rest.transaction.response.GetTransactionsResponse
import com.belcobtm.data.rest.transaction.response.ReceiverAccountActivatedResponse
import com.belcobtm.data.rest.transaction.response.TransactionDetailsResponse
import com.belcobtm.data.rest.transaction.response.hash.UtxoItemData
import com.belcobtm.data.rest.transaction.response.mapToDomainModel
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.transaction.item.SellPreSubmitDataItem
import com.belcobtm.domain.transaction.item.StakeDetailsDataItem
import com.belcobtm.domain.transaction.item.TransactionPlanItem
import com.belcobtm.domain.transaction.type.TransactionType
import com.belcobtm.domain.wallet.item.CoinDataItem

class TransactionApiService(
    private val api: TransactionApi,
    private val prefHelper: SharedPreferencesHelper,
    private val locationProvider: LocationProvider
) {

    suspend fun getTransactionPlan(coinCode: String): Either<Failure, TransactionPlanItem> = try {
        val request = api.getTransactionPlanAsync(prefHelper.userId, coinCode)
        request.body()?.let { body -> Either.Right(body.mapToDomainModel(coinCode)) }
            ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun fetchTransactions(coinCode: String): Either<Failure, GetTransactionsResponse> = try {
        val request = api.getTransactionsAsync(prefHelper.userId, coinCode)
        request.body()?.let { body -> Either.Right(body) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun receiverAccountActivated(
        coinCode: String, toAddress: String
    ): Either<Failure, ReceiverAccountActivatedResponse> = try {
        val request = api.receiverAccountActivatedAsync(coinCode, toAddress)
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
        toAddress: String?,
        price: Double,
        fiatAmount: Double,
    ): Either<Failure, TransactionDetailsResponse> = try {
        val location = locationProvider.getCurrentLocation()
        val requestBody = WithdrawRequest(
            hex = hash,
            fromAddress = fromAddress,
            toAddress = toAddress,
            cryptoAmount = coinFromAmount,
            fee = fee,
            price = price,
            latitude = location?.latitude,
            longitude = location?.longitude,
            fiatAmount = fiatAmount
        )
        val response = api.withdrawAsync(prefHelper.userId, coinFrom, requestBody)
        response.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun getGiftAddress(
        coinFrom: String,
        phone: String
    ): Either<Failure, String> = try {
        val request = api.getGiftAddressAsync(coinFrom, phone)
        request.body()?.let { Either.Right(it.address ?: "") } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun sendTransfer(
        hash: String,
        coinFrom: String,
        coinFromAmount: Double,
        giftId: String?,
        phone: String,
        message: String?,
        fee: Double? = null,
        feePercent: Int?,
        fiatAmount: Double,
        location: Location,
        fromAddress: String? = null,
        toAddress: String? = null
    ): Either<Failure, TransactionDetailsResponse> = try {
        val requestBody = SendGiftRequest(
            cryptoAmount = coinFromAmount,
            phone = phone,
            message = message,
            image = giftId,
            hex = hash,
            fee = fee,
            feePercent = feePercent,
            fiatAmount = fiatAmount,
            fromAddress = fromAddress,
            toAddress = toAddress,
            latitude = location.latitude,
            longitude = location.longitude
        )
        val request = api.sendGiftAsync(prefHelper.userId, coinFrom, requestBody)
        request.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
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
        )

        request.body()?.let { Either.Right(it.mapToDomainModel()) }
            ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun atmSell(
        coin: String,
        coinAmount: Double,
        usdAmount: Int,
        price: Double,
        fee: Double
    ): Either<Failure, TransactionDetailsResponse> = try {
        val location = locationProvider.getCurrentLocation()
        val requestBody = SellRequest(
            price = price,
            cryptoAmount = coinAmount,
            fiatAmount = usdAmount,
            feePercent = fee,
            latitude = location?.latitude,
            longitude = location?.longitude
        )
        val request = api.sellAsync(prefHelper.userId, coin, requestBody)
        request.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun swap(
        coinFromAmount: Double,
        coinToAmount: Double,
        coinFrom: CoinDataItem,
        coinTo: CoinDataItem,
        hash: String,
        fee: Double,
        fiatAmount: Double,
        fromAddress: String,
        toAddress: String,
        location: Location
    ): Either<Failure, TransactionDetailsResponse> = try {
        val requestBody = CoinToCoinExchangeRequest(
            hex = hash,
            fromAddress = fromAddress,
            toAddress = toAddress,
            cryptoAmount = coinFromAmount,
            price = coinFrom.priceUsd,
            refCoin = coinTo.code,
            refCoinPrice = coinTo.priceUsd,
            refCryptoAmount = coinToAmount,
            feePercent = fee,
            fiatAmount = fiatAmount,
            longitude = location.longitude,
            latitude = location.latitude
        )
        val request = api.exchangeAsync(prefHelper.userId, coinFrom.code, requestBody)
        request.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun getUtxoList(
        coinId: String,
        publicKey: String
    ): Either<Failure, List<UtxoItemData>> = try {
        val request = api.getUtxoListAsync(coinId, publicKey)
        request.body()?.utxos?.let { list ->
            Either.Right(list.map { it.mapToData() })
        } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun submitRecall(
        coinCode: String,
        cryptoAmount: Double,
        price: Double
    ): Either<Failure, TransactionDetailsResponse> = try {
        val location = locationProvider.getCurrentLocation()
        val request = TradeRecallRequest(
            cryptoAmount = cryptoAmount,
            price = price,
            latitude = location?.latitude,
            longitude = location?.longitude
        )
        val response = api.submitRecallAsync(prefHelper.userId, coinCode, request)
        response.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun submitReserve(
        coinCode: String, fromAddress: String,
        toAddress: String,
        cryptoAmount: Double,
        fee: Double,
        hex: String,
        price: Double
    ): Either<Failure, TransactionDetailsResponse> = try {
        val location = locationProvider.getCurrentLocation()
        val request = TradeReserveRequest(
            cryptoAmount = cryptoAmount,
            fromAddress = fromAddress,
            toAddress = toAddress,
            fee = fee,
            hex = hex,
            price = price,
            latitude = location?.latitude,
            longitude = location?.longitude
        )
        val response = api.submitReserveAsync(prefHelper.userId, coinCode, request)
        response.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun stakeDetails(coinCode: String): Either<Failure, StakeDetailsDataItem> = try {
        val request = api.stakeDetailsAsync(prefHelper.userId, coinCode)
        request.body()?.let { Either.Right(it.mapToDomainModel()) }
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
        feePercent: Double,
        fiatAMount: Double,
        hex: String,
        location: Location
    ): Either<Failure, TransactionDetailsResponse> = try {
        val requestBody = StakeRequest(
            type = TransactionType.CREATE_STAKE.toString(),
            fromAddress = fromAddress,
            toAddress = toAddress,
            cryptoAmount = cryptoAmount,
            fee = fee,
            hex = hex,
            feePercent = feePercent,
            fiatAmount = fiatAMount,
            longitude = location.longitude,
            latitude = location.latitude
        )
        val request = api.stakeOrUnStakeAsync(prefHelper.userId, coinCode, requestBody)
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
        hex: String,
        location: Location
    ): Either<Failure, TransactionDetailsResponse> = try {
        val requestBody = StakeRequest(
            type = TransactionType.CANCEL_STAKE.toString(),
            fromAddress = fromAddress,
            toAddress = toAddress,
            cryptoAmount = cryptoAmount,
            fee = fee,
            hex = hex,
            longitude = location.longitude,
            latitude = location.latitude
        )
        val request = api.stakeOrUnStakeAsync(prefHelper.userId, coinCode, requestBody)
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
        hex: String,
        location: Location
    ): Either<Failure, TransactionDetailsResponse> = try {
        val requestBody = StakeRequest(
            type = TransactionType.WITHDRAW_STAKE.toString(),
            fromAddress = fromAddress,
            toAddress = toAddress,
            cryptoAmount = cryptoAmount,
            fee = fee,
            hex = hex,
            longitude = location.longitude,
            latitude = location.latitude
        )
        val request = api.stakeOrUnStakeAsync(prefHelper.userId, coinCode, requestBody)
        request.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    companion object {

        const val UNIT_USD = "USD"
    }

}
