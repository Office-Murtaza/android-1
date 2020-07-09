package com.app.belcobtm.data.rest.transaction

import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.rest.transaction.request.*
import com.app.belcobtm.data.rest.transaction.response.hash.BinanceBlockResponse
import com.app.belcobtm.data.rest.transaction.response.hash.TronRawDataResponse
import com.app.belcobtm.data.rest.transaction.response.hash.UtxoItemResponse
import com.app.belcobtm.data.rest.transaction.response.mapToDataItem
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.transaction.item.*
import com.app.belcobtm.domain.transaction.type.TradeSortType

class TransactionApiService(
    private val api: TransactionApi,
    private val prefHelper: SharedPreferencesHelper
) {

    suspend fun getTransactions(
        coinCode: String,
        lastListIndex: Int
    ): Either<Failure, Pair<Int, List<TransactionDataItem>>> = try {
        val request = api.getTransactionsAsync(prefHelper.userId, coinCode, lastListIndex).await()
        request.body()?.let { body ->
            Either.Right(Pair(body.total, body.transactions.map { it.mapToDataItem() }))
        } ?: Either.Left(Failure.ServerError())
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

    suspend fun sellGetLimitsAsync(): Either<Failure, SellLimitsDataItem> = try {
        val request = api.sellGetLimitsAsync(prefHelper.userId).await()
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

    suspend fun getTradeInfo(coinFrom: String): Either<Failure, TradeInfoDataItem> = try {
        val request = api.tradeGetInfoAsync(prefHelper.userId, coinFrom).await()
        request.body()?.let { Either.Right(it.mapToDataItem()) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun getTradeBuyList(
        coinFrom: String,
        sortType: TradeSortType,
        paginationStep: Int
    ): Either<Failure, TradeInfoDataItem> = try {
        val request = api.getBuyTradeListAsync(prefHelper.userId, coinFrom, sortType.code, paginationStep).await()
        request.body()?.let { Either.Right(it.mapToDataItem()) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun getTradeSellList(
        coinFrom: String,
        sortType: TradeSortType,
        paginationStep: Int
    ): Either<Failure, TradeInfoDataItem> = try {
        val request = api.getSellTradeListAsync(prefHelper.userId, coinFrom, sortType.code, paginationStep).await()
        request.body()?.let { Either.Right(it.mapToDataItem()) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun getTradeMyList(
        coinFrom: String,
        sortType: TradeSortType,
        paginationStep: Int
    ): Either<Failure, TradeInfoDataItem> = try {
        val request = api.getMyTradeListAsync(prefHelper.userId, coinFrom, sortType.code, paginationStep).await()
        request.body()?.let { Either.Right(it.mapToDataItem()) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun getTradeOpenList(
        coinFrom: String,
        sortType: TradeSortType,
        paginationStep: Int
    ): Either<Failure, TradeInfoDataItem> = try {
        val request = api.getOpenTradeListAsync(prefHelper.userId, coinFrom, sortType.code, paginationStep).await()
        request.body()?.let { Either.Right(it.mapToDataItem()) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun tradeBuySell(
        id: Int,
        price: Int,
        fromUsdAmount: Int,
        toCoin: String,
        toCoinAmount: Double,
        detailsText: String
    ): Either<Failure, Unit> = try {
        val requestBody = TradeBuyRequest(id, price, fromUsdAmount, toCoinAmount, detailsText)
        val request = api.tradeBuySellAsync(prefHelper.userId, toCoin, requestBody).await()
        request.body()?.let { Either.Right(Unit) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun tradeBuyCreate(
        coinCode: String,
        paymentMethod: String,
        margin: Double,
        minLimit: Long,
        maxLimit: Long,
        terms: String
    ): Either<Failure, Unit> = try {
        val requestBody =
            TradeCreateRequest(TRANSACTION_TRADE_CREATE_BUY, paymentMethod, margin, minLimit, maxLimit, terms)
        val request = api.tradeCreateAsync(prefHelper.userId, coinCode, requestBody).await()
        request.body()?.let { Either.Right(Unit) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun tradeSellCreate(
        coinCode: String,
        paymentMethod: String,
        margin: Double,
        minLimit: Long,
        maxLimit: Long,
        terms: String
    ): Either<Failure, Unit> = try {
        val requestBody =
            TradeCreateRequest(TRANSACTION_TRADE_CREATE_SELL, paymentMethod, margin, minLimit, maxLimit, terms)
        val request = api.tradeCreateAsync(prefHelper.userId, coinCode, requestBody).await()
        request.body()?.let { Either.Right(Unit) } ?: Either.Left(Failure.ServerError())
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
        val request = api.getUtxoListAsync(coinId, publicKey).await()
        request.body()?.utxos?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun getEthereumNonce(coinCode: String, toAddress: String): Either<Failure, Long?> = try {
        val request = api.getEthereumNonceAsync(coinCode, toAddress).await()
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

    suspend fun getBinanceBlockHeader(toAddress: String): Either<Failure, BinanceBlockResponse> = try {
        val request = api.getBinanceBlockHeaderAsync(toAddress).await()
        request.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun getTronBlockHeader(coinId: String): Either<Failure, TronRawDataResponse?> = try {
        val request = api.getTronBlockHeaderAsync(coinId).await()
        request.body()?.let { Either.Right(it.blockHeader?.raw_data) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun submitRecall(coinCode: String, cryptoAmount: Double): Either<Failure, Unit> = try {
        val requestBody = TradeRecallRequest(TRANSACTION_TRADE_RECALL, cryptoAmount)
        val request = api.submitRecallAsync(prefHelper.userId, coinCode, requestBody).await()
        request.body()?.let { Either.Right(Unit) } ?: Either.Left(Failure.ServerError())
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
    ): Either<Failure, Unit> = try {
        val requestBody = TradeReserveRequest(TRANSACTION_TRADE_RESERVE, fromAddress, toAddress, cryptoAmount, fee, hex)
        val request = api.submitReserveAsync(prefHelper.userId, coinCode, requestBody).await()
        request.body()?.let { Either.Right(Unit) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun stakeDetails(coinCode: String): Either<Failure, StakeDetailsDataItem> = try {
        val request = api.stakeDetailsAsync(prefHelper.userId, coinCode).await()
        request.body()?.let { Either.Right(it.mapToDataItem()) } ?: Either.Left(Failure.ServerError())
    } catch (failure: Failure) {
        failure.printStackTrace()
        Either.Left(failure)
    }

    suspend fun stake(
        coinCode: String,
        fromAddress: String,
        toAddress: String,
        cryptoAmount: Double,
        fee: Double,
        hex: String
    ): Either<Failure, Unit> = try {
        val requestBody = StakeRequest(TRANSACTION_STAKE, fromAddress, toAddress, cryptoAmount, fee, hex)
        val request = api.stakeOrUnStakeAsync(prefHelper.userId, coinCode, requestBody).await()
        request.body()?.let { Either.Right(Unit) } ?: Either.Left(Failure.ServerError())
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
    ): Either<Failure, Unit> = try {
        val requestBody = StakeRequest(TRANSACTION_UNSTAKE, fromAddress, toAddress, cryptoAmount, fee, hex)
        val request = api.stakeOrUnStakeAsync(prefHelper.userId, coinCode, requestBody).await()
        request.body()?.let { Either.Right(Unit) } ?: Either.Left(Failure.ServerError())
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
        const val TRANSACTION_UNSTAKE = 14

        const val TRANSACTION_TRADE_CREATE_BUY = 1
        const val TRANSACTION_TRADE_CREATE_SELL = 2
        const val TRANSACTION_TRADE_RECALL = 11
        const val TRANSACTION_TRADE_RESERVE = 10
        const val UNIT_USD = "USD"
    }
}