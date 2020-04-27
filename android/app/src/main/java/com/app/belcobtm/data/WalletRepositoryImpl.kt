package com.app.belcobtm.data

import com.app.belcobtm.data.core.NetworkUtils
import com.app.belcobtm.data.core.TransactionHashHelper
import com.app.belcobtm.data.rest.wallet.WalletApiService
import com.app.belcobtm.data.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.CoinDataItem
import com.app.belcobtm.domain.wallet.CoinFeeDataItem
import com.app.belcobtm.domain.wallet.WalletRepository

class WalletRepositoryImpl(
    private val apiService: WalletApiService,
    private val prefHelper: SharedPreferencesHelper,
    private val transactionHashRepository: TransactionHashHelper,
    private val networkUtils: NetworkUtils
) : WalletRepository {
    override fun getCoinFeeMap(): Map<String, CoinFeeDataItem> = prefHelper.coinsFee

    override suspend fun sendSmsToDevice(): Either<Failure, Unit> = if (networkUtils.isNetworkAvailable()) {
        apiService.sendToDeviceSmsCode()
    } else {
        Either.Left(Failure.NetworkConnection)
    }

    override suspend fun createTransaction(
        fromCoin: CoinDataItem,
        fromCoinAmount: Double,
        isNeedSendSms: Boolean
    ): Either<Failure, String> = if (networkUtils.isNetworkAvailable()) {
        val toAddress = prefHelper.coinsFee[fromCoin.code]?.serverWalletAddress ?: ""
        val hashResponse = transactionHashRepository.createTransactionHash(fromCoin, fromCoinAmount, toAddress)
        when {
            isNeedSendSms && hashResponse.isRight -> {
                val sendSmsToDeviceResponse = apiService.sendToDeviceSmsCode()
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
        coinFrom: String,
        coinFromAmount: Double
    ): Either<Failure, Unit> = if (networkUtils.isNetworkAvailable()) {
        val smsCodeVerifyResponse = apiService.verifySmsCode(smsCode)
        if (smsCodeVerifyResponse.isRight) {
            apiService.withdraw(hash, coinFrom, coinFromAmount)
        } else {
            smsCodeVerifyResponse as Either.Left
        }
    } else {
        Either.Left(Failure.NetworkConnection)
    }


    override suspend fun getGiftAddress(
        coinFrom: String,
        phone: String
    ): Either<Failure, String> = if (networkUtils.isNetworkAvailable()) {
        apiService.getGiftAddress(coinFrom, phone)
    } else {
        Either.Left(Failure.NetworkConnection)
    }

    override suspend fun sendGift(
        smsCode: String,
        hash: String,
        coinFrom: String,
        coinFromAmount: Double,
        giftId: String,
        phone: String,
        message: String
    ): Either<Failure, Unit> = if (networkUtils.isNetworkAvailable()) {
        val smsCodeVerifyResponse = apiService.verifySmsCode(smsCode)
        if (smsCodeVerifyResponse.isRight) {
            apiService.sendGift(hash, coinFrom, coinFromAmount, giftId, phone, message)
        } else {
            smsCodeVerifyResponse as Either.Left
        }
    } else {
        Either.Left(Failure.NetworkConnection)
    }

    override suspend fun exchangeCoinToCoin(
        smsCode: String,
        coinFromAmount: Double,
        coinFrom: String,
        coinTo: String,
        hex: String
    ): Either<Failure, Unit> = if (networkUtils.isNetworkAvailable()) {
        val smsCodeVerifyResponse = apiService.verifySmsCode(smsCode)
        if (smsCodeVerifyResponse.isRight) {
            apiService.coinToCoinExchange(coinFromAmount, coinFrom, coinTo, hex)
        } else {
            smsCodeVerifyResponse as Either.Left
        }
    } else {
        Either.Left(Failure.NetworkConnection)
    }

}