package com.app.belcobtm.data

import com.app.belcobtm.data.core.NetworkUtils
import com.app.belcobtm.data.core.TransactionHashHelper
import com.app.belcobtm.data.disk.database.AccountDao
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.rest.transaction.TransactionApiService
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.tools.ToolsRepository
import com.app.belcobtm.domain.transaction.TransactionRepository
import com.app.belcobtm.domain.transaction.item.*
import com.app.belcobtm.domain.transaction.type.TradeSortType
import com.app.belcobtm.domain.wallet.LocalCoinType

class TransactionRepositoryImpl(
    private val apiService: TransactionApiService,
    private val prefHelper: SharedPreferencesHelper,
    private val toolsRepository: ToolsRepository,
    private val transactionHashRepository: TransactionHashHelper,
    private val networkUtils: NetworkUtils,
    private val daoAccount: AccountDao
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
                val sendSmsToDeviceResponse = toolsRepository.sendSmsToDeviceOld()
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

    override suspend fun withdraw(
        fromCoin: String,
        fromCoinAmount: Double,
        toAddress: String
    ): Either<Failure, Unit> = if (networkUtils.isNetworkAvailable()) {
        val coinType = LocalCoinType.valueOf(fromCoin)
        val hashResponse = transactionHashRepository.createTransactionHash(coinType, fromCoinAmount, toAddress)
        if (hashResponse.isRight) {
            val fee = prefHelper.coinsFee[fromCoin]?.txFee ?: 0.0
            val fromAddress = daoAccount.getItem(fromCoin).publicKey
            apiService.withdraw((hashResponse as Either.Right).b, fromCoin, fromCoinAmount, fee, fromAddress, toAddress)
        } else {
            hashResponse as Either.Left
        }
    } else {
        Either.Left(Failure.NetworkConnection)
    }

    override suspend fun sendGift(
        amount: Double,
        coinCode: String,
        giftId: String,
        phone: String,
        message: String
    ): Either<Failure, Unit> {
        val giftAddressResponse = apiService.getGiftAddress(coinCode, phone)
        return if (giftAddressResponse.isRight) {
            val coinType = LocalCoinType.valueOf(coinCode)
            val toAddress = (giftAddressResponse as Either.Right).b
            val hashResponse = transactionHashRepository.createTransactionHash(coinType, amount, toAddress)
            if (hashResponse.isRight) {
                val fee = prefHelper.coinsFee[coinCode]?.txFee ?: 0.0
                val fromAddress = daoAccount.getItem(coinCode).publicKey
                val hash = (hashResponse as Either.Right).b
                apiService.sendGift(hash, coinCode, amount, giftId, phone, message, fee, fromAddress, toAddress)
            } else {
                hashResponse as Either.Left
            }
        } else {
            giftAddressResponse as Either.Left
        }
    }

    override suspend fun sellGetLimits(): Either<Failure, SellLimitsDataItem> = if (networkUtils.isNetworkAvailable()) {
        apiService.sellGetLimitsAsync()
    } else {
        Either.Left(Failure.NetworkConnection)
    }

    override suspend fun sellPreSubmit(
        smsCode: String,
        fromCoin: String,
        cryptoAmount: Double,
        toUsdAmount: Int
    ): Either<Failure, SellPreSubmitDataItem> = if (networkUtils.isNetworkAvailable()) {
        val smsCodeVerifyResponse = toolsRepository.verifySmsCodeOld(smsCode)
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

    override suspend fun exchange(
        fromCoinAmount: Double,
        fromCoin: String,
        coinTo: String
    ): Either<Failure, Unit> {
        val coinType = LocalCoinType.valueOf(fromCoin)
        val toAddress = daoAccount.getItem(coinTo).publicKey
        val hashResponse = transactionHashRepository.createTransactionHash(coinType, fromCoinAmount, toAddress)
        return if (hashResponse.isRight) {
            val fee = prefHelper.coinsFee[fromCoin]?.txFee ?: 0.0
            val fromAddress = daoAccount.getItem(fromCoin).publicKey
            val hash = (hashResponse as Either.Right).b
            apiService.exchange(fromCoinAmount, fromCoin, coinTo, hash, fee, fromAddress, toAddress)
        } else {
            hashResponse as Either.Left
        }
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

    override suspend fun tradeRecallTransactionCreate(
        coinCode: String,
        cryptoAmount: Double
    ): Either<Failure, Unit> = if (networkUtils.isNetworkAvailable()) {
        toolsRepository.sendSmsToDeviceOld()
    } else {
        Either.Left(Failure.NetworkConnection)
    }

    override suspend fun tradeRecallTransactionComplete(
        smsCode: String,
        coinCode: String,
        cryptoAmount: Double
    ): Either<Failure, Unit> = if (networkUtils.isNetworkAvailable()) {
        val smsCodeVerifyResponse = toolsRepository.verifySmsCodeOld(smsCode)
        if (smsCodeVerifyResponse.isRight) {
            apiService.submitRecall(coinCode, cryptoAmount)
        } else {
            smsCodeVerifyResponse as Either.Left
        }
    } else {
        Either.Left(Failure.NetworkConnection)
    }

    override suspend fun tradeReserveTransactionCreate(
        coinCode: String,
        cryptoAmount: Double
    ): Either<Failure, String> = if (networkUtils.isNetworkAvailable()) {
        val toAddress = prefHelper.coinsFee[coinCode]?.walletAddress ?: ""
        val coinType = LocalCoinType.valueOf(coinCode)
        val hashResponse = transactionHashRepository.createTransactionHash(coinType, cryptoAmount, toAddress)
        val sendSmsToDeviceResponse = toolsRepository.sendSmsToDeviceOld()
        when {
            hashResponse.isRight && sendSmsToDeviceResponse.isRight -> hashResponse as Either.Right
            sendSmsToDeviceResponse.isLeft -> sendSmsToDeviceResponse as Either.Left
            else -> hashResponse as Either.Left
        }
    } else {
        Either.Left(Failure.NetworkConnection)
    }

    override suspend fun tradeReserveTransactionComplete(
        smsCode: String,
        coinCode: String,
        cryptoAmount: Double,
        hash: String
    ): Either<Failure, Unit> = if (networkUtils.isNetworkAvailable()) {
        val smsCodeVerifyResponse = toolsRepository.verifySmsCodeOld(smsCode)
        if (smsCodeVerifyResponse.isRight) {
            val fromAddress = prefHelper.coinsFee[coinCode]?.walletAddress ?: ""
            val toAddress = prefHelper.coinsFee[coinCode]?.contractAddress ?: ""
            val fee = prefHelper.coinsFee[coinCode]?.txFee ?: 0.0
            apiService.submitReserve(coinCode, fromAddress, toAddress, cryptoAmount, fee, hash)
        } else {
            smsCodeVerifyResponse as Either.Left
        }
    } else {
        Either.Left(Failure.NetworkConnection)
    }

    override suspend fun stakeDetails(
        coinCode: String
    ): Either<Failure, StakeDetailsDataItem> = if (networkUtils.isNetworkAvailable()) {
        apiService.stakeDetails(coinCode)
    } else {
        Either.Left(Failure.NetworkConnection)
    }

    override suspend fun stakeCreateTransaction(
        coinCode: String,
        cryptoAmount: Double
    ): Either<Failure, String> = if (networkUtils.isNetworkAvailable()) {
        val toAddress = prefHelper.coinsFee[coinCode]?.walletAddress ?: ""
        val hashResponse = transactionHashRepository.createTransactionStakeHash(cryptoAmount, toAddress)
        val sendSmsToDeviceResponse = toolsRepository.sendSmsToDeviceOld()
        when {
            hashResponse.isRight && sendSmsToDeviceResponse.isRight -> hashResponse as Either.Right
            sendSmsToDeviceResponse.isLeft -> sendSmsToDeviceResponse as Either.Left
            else -> hashResponse as Either.Left
        }
    } else {
        Either.Left(Failure.NetworkConnection)
    }

    override suspend fun stakeCompleteTransaction(
        smsCode: String,
        hash: String,
        coinCode: String,
        cryptoAmount: Double
    ): Either<Failure, Unit> = if (networkUtils.isNetworkAvailable()) {
        val smsCodeVerifyResponse = toolsRepository.verifySmsCodeOld(smsCode)
        if (smsCodeVerifyResponse.isRight) {
            val fromAddress = prefHelper.coinsFee[coinCode]?.walletAddress ?: ""
            val toAddress = prefHelper.coinsFee[coinCode]?.contractAddress ?: ""
            val coinFee = prefHelper.coinsFee[coinCode]?.txFee ?: 0.0
            apiService.stake(coinCode, fromAddress, toAddress, cryptoAmount, coinFee, hash)
        } else {
            smsCodeVerifyResponse as Either.Left
        }
    } else {
        Either.Left(Failure.NetworkConnection)
    }

    override suspend fun unStakeCreateTransaction(
        coinCode: String,
        cryptoAmount: Double
    ): Either<Failure, String> = if (networkUtils.isNetworkAvailable()) {
        val toAddress = prefHelper.coinsFee[coinCode]?.walletAddress ?: ""
        val hashResponse = transactionHashRepository.createTransactionUnStakeHash(cryptoAmount, toAddress)
        val sendSmsToDeviceResponse = toolsRepository.sendSmsToDeviceOld()
        when {
            hashResponse.isRight && sendSmsToDeviceResponse.isRight -> hashResponse as Either.Right
            sendSmsToDeviceResponse.isLeft -> sendSmsToDeviceResponse as Either.Left
            else -> hashResponse as Either.Left
        }
    } else {
        Either.Left(Failure.NetworkConnection)
    }

    override suspend fun unStakeCompleteTransaction(
        smsCode: String,
        hash: String,
        coinCode: String,
        cryptoAmount: Double
    ): Either<Failure, Unit> = if (networkUtils.isNetworkAvailable()) {
        val smsCodeVerifyResponse = toolsRepository.verifySmsCodeOld(smsCode)
        if (smsCodeVerifyResponse.isRight) {
            val fromAddress = prefHelper.coinsFee[coinCode]?.walletAddress ?: ""
            val toAddress = prefHelper.coinsFee[coinCode]?.contractAddress ?: ""
            val coinFee = prefHelper.coinsFee[coinCode]?.txFee ?: 0.0
            apiService.unStake(coinCode, fromAddress, toAddress, cryptoAmount, coinFee, hash)
        } else {
            smsCodeVerifyResponse as Either.Left
        }
    } else {
        Either.Left(Failure.NetworkConnection)
    }

    override suspend fun getTransactionDetails(
        txId: String,
        coinCode: String
    ): Either<Failure, TransactionDetailsDataItem> = apiService.getTransactionDetails(txId, coinCode)
}