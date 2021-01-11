package com.app.belcobtm.data

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
import com.app.belcobtm.domain.wallet.item.isEthRelatedCoinCode

class TransactionRepositoryImpl(
    private val apiService: TransactionApiService,
    private val prefHelper: SharedPreferencesHelper,
    private val toolsRepository: ToolsRepository,
    private val transactionHashRepository: TransactionHashHelper,
    private val daoAccount: AccountDao
) : TransactionRepository {

    override suspend fun getTransactionList(
        coinCode: String,
        currentListSize: Int
    ): Either<Failure, Pair<Int, List<TransactionDataItem>>> = apiService.getTransactions(coinCode, currentListSize)

    override suspend fun createTransaction(
        fromCoin: String,
        fromCoinAmount: Double,
        isNeedSendSms: Boolean
    ): Either<Failure, String> {
        val toAddress = prefHelper.coinsDetails[fromCoin]?.walletAddress ?: ""
        val coinType = LocalCoinType.valueOf(fromCoin)
        val hashResponse =
            transactionHashRepository.createTransactionHash(coinType, fromCoinAmount, toAddress)
        return when {
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
    }

    override suspend fun withdraw(
        fromCoin: String,
        fromCoinAmount: Double,
        toAddress: String
    ): Either<Failure, Unit> {
        val coinType = LocalCoinType.valueOf(fromCoin)
        val hashResponse =
            transactionHashRepository.createTransactionHash(coinType, fromCoinAmount, toAddress)
        return if (hashResponse.isRight) {
            val fee = prefHelper.coinsDetails[fromCoin]?.txFee ?: 0.0
            val fromAddress = daoAccount.getItem(fromCoin).publicKey
            apiService.withdraw(
                (hashResponse as Either.Right).b,
                fromCoin,
                fromCoinAmount,
                fee,
                fromAddress,
                toAddress
            )
        } else {
            hashResponse as Either.Left
        }
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
            val hashResponse =
                transactionHashRepository.createTransactionHash(coinType, amount, toAddress)
            if (hashResponse.isRight) {
                val fee = prefHelper.coinsDetails[coinCode]?.txFee ?: 0.0
                val fromAddress = daoAccount.getItem(coinCode).publicKey
                val hash = (hashResponse as Either.Right).b
                if (coinCode == LocalCoinType.ETH.name || coinCode == LocalCoinType.CATM.name) {
                    apiService.sendGift(
                        hash,
                        coinCode,
                        amount,
                        giftId,
                        phone,
                        message,
                        fee,
                        fromAddress,
                        toAddress
                    )
                } else {
                    apiService.sendGift(hash, coinCode, amount, giftId, phone, message)
                }

            } else {
                hashResponse as Either.Left
            }
        } else {
            giftAddressResponse as Either.Left
        }
    }

    override suspend fun sellGetLimits(): Either<Failure, SellLimitsDataItem> = apiService.sellGetLimitsAsync()

    override suspend fun sellPreSubmit(
        smsCode: String,
        fromCoin: String,
        cryptoAmount: Double,
        toUsdAmount: Int
    ): Either<Failure, SellPreSubmitDataItem> {
        val smsCodeVerifyResponse = toolsRepository.verifySmsCodeOld(smsCode)
        return if (smsCodeVerifyResponse.isRight) {
            apiService.sellPreSubmit(fromCoin, cryptoAmount, toUsdAmount)
        } else {
            smsCodeVerifyResponse as Either.Left
        }
    }

    override suspend fun sell(
        fromCoin: String,
        fromCoinAmount: Double
    ): Either<Failure, Unit> {
        val transactionResponse = createTransaction(fromCoin, fromCoinAmount, false)
        return if (transactionResponse.isRight) {
            val hash = (transactionResponse as Either.Right).b
            apiService.sell(fromCoinAmount, fromCoin, hash)
        } else {
            transactionResponse as Either.Left
        }
    }

    override suspend fun exchange(
        fromCoinAmount: Double,
        toCoinAmount: Double,
        fromCoin: String,
        coinTo: String
    ): Either<Failure, Unit> {
        val coinType = LocalCoinType.valueOf(fromCoin)
        val toAddress: String = if (fromCoin.isEthRelatedCoinCode()) {
            prefHelper.coinsDetails[fromCoin]?.contractAddress
        } else {
            prefHelper.coinsDetails[fromCoin]?.walletAddress
        } ?: ""
        val hashResponse =
            transactionHashRepository.createTransactionHash(coinType, fromCoinAmount, toAddress)
        return if (hashResponse.isRight) {
            val toAddressSend = prefHelper.coinsDetails[fromCoin]?.walletAddress ?: ""
            val fee = prefHelper.coinsDetails[fromCoin]?.txFee ?: 0.0
            val fromAddress = daoAccount.getItem(fromCoin).publicKey
            val hash = (hashResponse as Either.Right).b
            apiService.exchange(
                fromCoinAmount,
                toCoinAmount,
                fromCoin,
                coinTo,
                hash,
                fee,
                fromAddress,
                toAddressSend
            )
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
    ): Either<Failure, Unit> = apiService.tradeBuySell(id, price, fromUsdAmount, toCoin, toCoinAmount, detailsText)

    override suspend fun tradeBuyCreate(
        coinCode: String,
        paymentMethod: String,
        margin: Double,
        minLimit: Long,
        maxLimit: Long,
        terms: String
    ): Either<Failure, Unit> = apiService.tradeBuyCreate(coinCode, paymentMethod, margin, minLimit, maxLimit, terms)

    override suspend fun tradeSellCreate(
        coinCode: String,
        paymentMethod: String,
        margin: Double,
        minLimit: Long,
        maxLimit: Long,
        terms: String
    ): Either<Failure, Unit> = apiService.tradeSellCreate(coinCode, paymentMethod, margin, minLimit, maxLimit, terms)

    override suspend fun tradeRecallTransactionComplete(
        coinCode: String,
        cryptoAmount: Double
    ): Either<Failure, Unit> {
        return apiService.submitRecall(coinCode, cryptoAmount)
    }

    override suspend fun tradeReserveTransactionCreate(
        coinCode: String,
        cryptoAmount: Double
    ): Either<Failure, String> {
        val toAddress = prefHelper.coinsDetails[coinCode]?.walletAddress ?: ""
        val coinType = LocalCoinType.valueOf(coinCode)
        val hashResponse =
            transactionHashRepository.createTransactionHash(coinType, cryptoAmount, toAddress)
        return when {
            hashResponse.isRight -> hashResponse as Either.Right
            else -> hashResponse as Either.Left
        }
    }

    override suspend fun tradeReserveTransactionComplete(
        coinCode: String,
        cryptoAmount: Double,
        hash: String
    ): Either<Failure, Unit> {
        val fromAddress = prefHelper.coinsDetails[coinCode]?.walletAddress ?: ""
        val toAddress = prefHelper.coinsDetails[coinCode]?.contractAddress ?: ""
        val fee = prefHelper.coinsDetails[coinCode]?.txFee ?: 0.0
        return apiService.submitReserve(coinCode, fromAddress, toAddress, cryptoAmount, fee, hash)
    }
    override suspend fun stakeDetails(
        coinCode: String
    ): Either<Failure, StakeDetailsDataItem> = apiService.stakeDetails(coinCode)

    override suspend fun stakeCreate(
        coinCode: String,
        cryptoAmount: Double
    ): Either<Failure, Unit> {
        val transactionResponse = transactionHashRepository.createTransactionStakeHash(
            cryptoAmount,
            prefHelper.coinsDetails[coinCode]?.walletAddress ?: ""
        )
        return if (transactionResponse.isRight) {
            val hash = (transactionResponse as Either.Right).b
            val fromAddress = prefHelper.coinsDetails[coinCode]?.walletAddress ?: ""
            val toAddress = prefHelper.coinsDetails[coinCode]?.contractAddress ?: ""
            val coinFee = prefHelper.coinsDetails[coinCode]?.txFee ?: 0.0
            apiService.stakeCreate(coinCode, fromAddress, toAddress, cryptoAmount, coinFee, hash)
        } else {
            transactionResponse as Either.Left
        }
    }

    override suspend fun stakeCancel(
        coinCode: String
    ): Either<Failure, Unit> {
        val transactionResponse = transactionHashRepository.createTransactionStakeCancelHash(
            0.0,
            prefHelper.coinsDetails[coinCode]?.walletAddress ?: ""
        )

        return if (transactionResponse.isRight) {
            val hash = (transactionResponse as Either.Right).b
            val fromAddress = prefHelper.coinsDetails[coinCode]?.walletAddress ?: ""
            val toAddress = prefHelper.coinsDetails[coinCode]?.contractAddress ?: ""
            val coinFee = prefHelper.coinsDetails[coinCode]?.txFee ?: 0.0
            apiService.stakeCancel(coinCode, fromAddress, toAddress, 0.0, coinFee, hash)
        } else {
            transactionResponse as Either.Left
        }
    }

    override suspend fun stakeWithdraw(
        coinCode: String,
        cryptoAmount: Double
    ): Either<Failure, Unit> {
        val transactionResponse = transactionHashRepository.createTransactionUnStakeHash(
            cryptoAmount,
            prefHelper.coinsDetails[coinCode]?.walletAddress ?: ""
        )

        return if (transactionResponse.isRight) {
            val hash = (transactionResponse as Either.Right).b
            val fromAddress = prefHelper.coinsDetails[coinCode]?.walletAddress ?: ""
            val toAddress = prefHelper.coinsDetails[coinCode]?.contractAddress ?: ""
            val coinFee = prefHelper.coinsDetails[coinCode]?.txFee ?: 0.0
            apiService.unStake(coinCode, fromAddress, toAddress, cryptoAmount, coinFee, hash)
        } else {
            transactionResponse as Either.Left
        }
    }

    override suspend fun getTransactionDetails(
        txId: String,
        coinCode: String
    ): Either<Failure, TransactionDetailsDataItem> = apiService.getTransactionDetails(txId, coinCode)

    override suspend fun checkXRPAddressActivated(address: String): Either<Failure, Boolean> {
        return apiService.getXRPAddressActivated(address)
    }
}
