package com.app.belcobtm.data

import com.app.belcobtm.data.core.NetworkUtils
import com.app.belcobtm.data.core.TransactionHashHelper
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.rest.transaction.TransactionApiService
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.tools.ToolsRepository
import com.app.belcobtm.domain.transaction.TransactionRepository
import com.app.belcobtm.domain.transaction.item.SellLimitsDataItem
import com.app.belcobtm.domain.transaction.item.SellPreSubmitDataItem
import com.app.belcobtm.domain.transaction.item.TradeInfoDataItem
import com.app.belcobtm.domain.transaction.item.TransactionDataItem
import com.app.belcobtm.domain.transaction.type.TradeSortType
import com.app.belcobtm.domain.wallet.LocalCoinType

class TransactionRepositoryImpl(
    private val apiService: TransactionApiService,
    private val prefHelper: SharedPreferencesHelper,
    private val toolsRepository: ToolsRepository,
    private val transactionHashRepository: TransactionHashHelper,
    private val networkUtils: NetworkUtils
) : TransactionRepository {

    override suspend fun getTransactionList(
        coinCode: String,
        currentListSize: Int
    ): Either<Failure, Pair<Int, List<TransactionDataItem>>> = if (networkUtils.isNetworkAvailable()) {
        apiService.getTransactions(coinCode, currentListSize)
    } else {
        Either.Left(Failure.NetworkConnection)
    }

    override suspend fun createTransaction(
        fromCoin: String,
        fromCoinAmount: Double,
        isNeedSendSms: Boolean
    ): Either<Failure, String> = if (networkUtils.isNetworkAvailable()) {
        val toAddress = prefHelper.coinsFee[fromCoin]?.walletAddress ?: ""
        val coinType = LocalCoinType.valueOf(fromCoin)
        val hashResponse = transactionHashRepository.createTransactionHash(coinType, fromCoinAmount, toAddress)
        when {
            isNeedSendSms && hashResponse.isRight -> {
                val sendSmsToDeviceResponse = toolsRepository.sendSmsToDevice()
                if (sendSmsToDeviceResponse.isRight) {
                    hashResponse as Either.Right
                } else {
                    sendSmsToDeviceResponse as Either.Left
                }
            }
            !isNeedSendSms && hashResponse.isRight -> hashResponse as Either.Right
            else -> hashResponse as Either.Left
        }
    } else {
        Either.Left(Failure.NetworkConnection)
    }

    override suspend fun createWithdrawTransaction(
        fromCoin: String,
        fromCoinAmount: Double,
        toAddress: String
    ): Either<Failure, String> = if (networkUtils.isNetworkAvailable()) {
        val coinType = LocalCoinType.valueOf(fromCoin)
        val hashResponse = transactionHashRepository.createTransactionHash(coinType, fromCoinAmount, toAddress)
        val sendSmsToDeviceResponse = toolsRepository.sendSmsToDevice()
        when {
            hashResponse.isRight && sendSmsToDeviceResponse.isRight -> hashResponse as Either.Right
            sendSmsToDeviceResponse.isLeft -> sendSmsToDeviceResponse as Either.Left
            else -> hashResponse as Either.Left
        }
    } else {
        Either.Left(Failure.NetworkConnection)
    }

    override suspend fun withdraw(
        smsCode: String,
        hash: String,
        fromCoin: String,
        fromCoinAmount: Double
    ): Either<Failure, Unit> = if (networkUtils.isNetworkAvailable()) {
        val smsCodeVerifyResponse = toolsRepository.verifySmsCode(smsCode)
        if (smsCodeVerifyResponse.isRight) {
            apiService.withdraw(hash, fromCoin, fromCoinAmount)
        } else {
            smsCodeVerifyResponse as Either.Left
        }
    } else {
        Either.Left(Failure.NetworkConnection)
    }

    override suspend fun getGiftAddress(
        fromCoin: String,
        phone: String
    ): Either<Failure, String> = if (networkUtils.isNetworkAvailable()) {
        apiService.getGiftAddress(fromCoin, phone)
    } else {
        Either.Left(Failure.NetworkConnection)
    }

    override suspend fun sendGift(
        smsCode: String,
        hash: String,
        fromCoin: String,
        fromCoinAmount: Double,
        giftId: String,
        phone: String,
        message: String
    ): Either<Failure, Unit> = if (networkUtils.isNetworkAvailable()) {
        val smsCodeVerifyResponse = toolsRepository.verifySmsCode(smsCode)
        if (smsCodeVerifyResponse.isRight) {
            apiService.sendGift(hash, fromCoin, fromCoinAmount, giftId, phone, message)
        } else {
            smsCodeVerifyResponse as Either.Left
        }
    } else {
        Either.Left(Failure.NetworkConnection)
    }

    override suspend fun sellGetLimits(
        fromCoin: String
    ): Either<Failure, SellLimitsDataItem> = if (networkUtils.isNetworkAvailable()) {
        apiService.sellGetLimitsAsync(fromCoin)
    } else {
        Either.Left(Failure.NetworkConnection)
    }

    override suspend fun sellPreSubmit(
        smsCode: String,
        fromCoin: String,
        cryptoAmount: Double,
        toUsdAmount: Int
    ): Either<Failure, SellPreSubmitDataItem> = if (networkUtils.isNetworkAvailable()) {
        val smsCodeVerifyResponse = toolsRepository.verifySmsCode(smsCode)
        if (smsCodeVerifyResponse.isRight) {
            apiService.sellPreSubmit(fromCoin, cryptoAmount, toUsdAmount)
        } else {
            smsCodeVerifyResponse as Either.Left
        }
    } else {
        Either.Left(Failure.NetworkConnection)
    }

    override suspend fun sell(
        fromCoin: String,
        fromCoinAmount: Double
    ): Either<Failure, Unit> = if (networkUtils.isNetworkAvailable()) {
        val transactionResponse = createTransaction(fromCoin, fromCoinAmount, false)
        if (transactionResponse.isRight) {
            val hash = (transactionResponse as Either.Right).b
            apiService.sell(fromCoinAmount, fromCoin, hash)
        } else {
            transactionResponse as Either.Left
        }
    } else {
        Either.Left(Failure.NetworkConnection)
    }

    override suspend fun exchangeCoinToCoin(
        smsCode: String,
        fromCoinAmount: Double,
        fromCoin: String,
        coinTo: String,
        hex: String
    ): Either<Failure, Unit> = if (networkUtils.isNetworkAvailable()) {
        val smsCodeVerifyResponse = toolsRepository.verifySmsCode(smsCode)
        if (smsCodeVerifyResponse.isRight) {
            apiService.coinToCoinExchange(fromCoinAmount, fromCoin, coinTo, hex)
        } else {
            smsCodeVerifyResponse as Either.Left
        }
    } else {
        Either.Left(Failure.NetworkConnection)
    }

    override suspend fun tradeGetBuyList(
        latitude: Double,
        longitude: Double,
        coinFrom: String,
        sortType: TradeSortType,
        paginationStep: Int
    ): Either<Failure, TradeInfoDataItem> = when {
        !networkUtils.isNetworkAvailable() -> Either.Left(Failure.NetworkConnection)
        (latitude > 0 || longitude > 0) && prefHelper.tradeLocationExpirationTime < System.currentTimeMillis() -> {
            val locationRequest = apiService.sendTradeUserLocation(latitude, longitude)
            prefHelper.tradeLocationExpirationTime =
                if (locationRequest.isRight) System.currentTimeMillis() else -1
            apiService.getTradeBuyList(coinFrom, sortType, paginationStep)
        }
        else -> apiService.getTradeBuyList(coinFrom, sortType, paginationStep)
    }

    override suspend fun getTradeSellList(
        latitude: Double,
        longitude: Double,
        coinFrom: String,
        sortType: TradeSortType,
        paginationStep: Int
    ): Either<Failure, TradeInfoDataItem> = when {
        !networkUtils.isNetworkAvailable() -> Either.Left(Failure.NetworkConnection)
        (latitude > 0 || longitude > 0) && prefHelper.tradeLocationExpirationTime < System.currentTimeMillis() -> {
            val locationRequest = apiService.sendTradeUserLocation(latitude, longitude)
            prefHelper.tradeLocationExpirationTime =
                if (locationRequest.isRight) System.currentTimeMillis() else -1
            apiService.getTradeSellList(coinFrom, sortType, paginationStep)
        }
        else -> apiService.getTradeSellList(coinFrom, sortType, paginationStep)
    }

    override suspend fun getTradeMyList(
        latitude: Double,
        longitude: Double,
        coinFrom: String,
        sortType: TradeSortType,
        paginationStep: Int
    ): Either<Failure, TradeInfoDataItem> = when {
        !networkUtils.isNetworkAvailable() -> Either.Left(Failure.NetworkConnection)
        (latitude > 0 || longitude > 0) && prefHelper.tradeLocationExpirationTime < System.currentTimeMillis() -> {
            val locationRequest = apiService.sendTradeUserLocation(latitude, longitude)
            prefHelper.tradeLocationExpirationTime =
                if (locationRequest.isRight) System.currentTimeMillis() else -1
            apiService.getTradeMyList(coinFrom, sortType, paginationStep)
        }
        else -> apiService.getTradeMyList(coinFrom, sortType, paginationStep)
    }

    override suspend fun getTradeOpenList(
        latitude: Double,
        longitude: Double,
        coinFrom: String,
        sortType: TradeSortType,
        paginationStep: Int
    ): Either<Failure, TradeInfoDataItem> = when {
        !networkUtils.isNetworkAvailable() -> Either.Left(Failure.NetworkConnection)
        (latitude > 0 || longitude > 0) && prefHelper.tradeLocationExpirationTime < System.currentTimeMillis() -> {
            val locationRequest = apiService.sendTradeUserLocation(latitude, longitude)
            prefHelper.tradeLocationExpirationTime =
                if (locationRequest.isRight) System.currentTimeMillis() else -1
            apiService.getTradeOpenList(coinFrom, sortType, paginationStep)
        }
        else -> apiService.getTradeOpenList(coinFrom, sortType, paginationStep)
    }

    override suspend fun tradeBuySell(
        id: Int,
        price: Int,
        fromUsdAmount: Int,
        toCoin: String,
        toCoinAmount: Double,
        detailsText: String
    ): Either<Failure, Unit> = if (networkUtils.isNetworkAvailable()) {
        apiService.tradeBuySell(id, price, fromUsdAmount, toCoin, toCoinAmount, detailsText)
    } else {
        Either.Left(Failure.NetworkConnection)
    }

    override suspend fun tradeBuyCreate(
        coinCode: String,
        paymentMethod: String,
        margin: Double,
        minLimit: Long,
        maxLimit: Long,
        terms: String
    ): Either<Failure, Unit> = if (networkUtils.isNetworkAvailable()) {
        apiService.tradeBuyCreate(coinCode, paymentMethod, margin, minLimit, maxLimit, terms)
    } else {
        Either.Left(Failure.NetworkConnection)
    }

    override suspend fun tradeSellCreate(
        coinCode: String,
        paymentMethod: String,
        margin: Double,
        minLimit: Long,
        maxLimit: Long,
        terms: String
    ): Either<Failure, Unit> = if (networkUtils.isNetworkAvailable()) {
        apiService.tradeSellCreate(coinCode, paymentMethod, margin, minLimit, maxLimit, terms)
    } else {
        Either.Left(Failure.NetworkConnection)
    }
}