package com.app.belcobtm.data

import com.app.belcobtm.data.core.NetworkUtils
import com.app.belcobtm.data.core.TransactionHashHelper
import com.app.belcobtm.data.disk.database.CoinDao
import com.app.belcobtm.data.disk.database.mapToDataItem
import com.app.belcobtm.data.disk.database.mapToEntity
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.rest.wallet.WalletApiService
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.domain.wallet.item.CoinFeeDataItem
import com.app.belcobtm.domain.wallet.WalletRepository
import com.app.belcobtm.domain.wallet.item.SellLimitsDataItem
import com.app.belcobtm.domain.wallet.item.SellPreSubmitDataItem
import com.app.belcobtm.presentation.core.extensions.CoinTypeExtension

class WalletRepositoryImpl(
    private val apiService: WalletApiService,
    private val prefHelper: SharedPreferencesHelper,
    private val transactionHashRepository: TransactionHashHelper,
    private val networkUtils: NetworkUtils,
    private val daoCoin: CoinDao
) : WalletRepository {
    override fun getCoinFeeMap(): Map<String, CoinFeeDataItem> = prefHelper.coinsFee

    override suspend fun getCoinList(): Either<Failure, List<CoinDataItem>> =
        Either.Right((daoCoin.getItemList() ?: emptyList()).map { it.mapToDataItem() })

    override suspend fun updateCoin(dataItem: CoinDataItem): Either<Failure, Unit> {
        daoCoin.updateItem(dataItem.mapToEntity())
        return Either.Right(Unit)
    }

    override suspend fun sendSmsToDevice(): Either<Failure, Unit> = if (networkUtils.isNetworkAvailable()) {
        apiService.sendToDeviceSmsCode()
    } else {
        Either.Left(Failure.NetworkConnection)
    }

    override suspend fun verifySmsCode(
        smsCode: String
    ): Either<Failure, Unit> = if (networkUtils.isNetworkAvailable()) {
        apiService.verifySmsCode(smsCode)
    } else {
        Either.Left(Failure.NetworkConnection)
    }

    override suspend fun createTransaction(
        fromCoin: String,
        fromCoinAmount: Double,
        isNeedSendSms: Boolean
    ): Either<Failure, String> = if (networkUtils.isNetworkAvailable()) {
        val toAddress = prefHelper.coinsFee[fromCoin]?.serverWalletAddress ?: ""
        val coinType = CoinTypeExtension.getTypeByCode(fromCoin)
        val hashResponse = transactionHashRepository.createTransactionHash(coinType!!, fromCoinAmount, toAddress)
        when {
            isNeedSendSms && hashResponse.isRight -> {
                val sendSmsToDeviceResponse = sendSmsToDevice()
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
        smsCode: String,
        hash: String,
        fromCoin: String,
        fromCoinAmount: Double
    ): Either<Failure, Unit> = if (networkUtils.isNetworkAvailable()) {
        val smsCodeVerifyResponse = verifySmsCode(smsCode)
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
        val smsCodeVerifyResponse = verifySmsCode(smsCode)
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
        val smsCodeVerifyResponse = verifySmsCode(smsCode)
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
        val smsCodeVerifyResponse = verifySmsCode(smsCode)
        if (smsCodeVerifyResponse.isRight) {
            apiService.coinToCoinExchange(fromCoinAmount, fromCoin, coinTo, hex)
        } else {
            smsCodeVerifyResponse as Either.Left
        }
    } else {
        Either.Left(Failure.NetworkConnection)
    }
}