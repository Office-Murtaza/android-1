package com.app.belcobtm.data

import com.app.belcobtm.data.core.TransactionHashHelper
import com.app.belcobtm.data.rest.wallet.WalletApiService
import com.app.belcobtm.data.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.db.DbCryptoCoinModel
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.CoinFeeDataItem
import com.app.belcobtm.domain.wallet.WalletRepository
import com.app.belcobtm.presentation.core.extensions.CoinTypeExtension
import com.app.belcobtm.presentation.core.extensions.code
import io.realm.Realm

class WalletRepositoryImpl(
    private val apiService: WalletApiService,
    private val prefHelper: SharedPreferencesHelper,
    private val transactionHashRepository: TransactionHashHelper
) : WalletRepository {
    override fun getCoinFeeMap(): Map<String, CoinFeeDataItem> = prefHelper.coinsFee

    override suspend fun sendSmsToDevice(): Either<Failure, Unit> = apiService.sendToDeviceSmsCode()

    override suspend fun createTransaction(
        fromCoinCode: String,
        fromCoinAmount: Double,
        isNeedSendSms: Boolean
    ): Either<Failure, String> = CoinTypeExtension.getTypeByCode(fromCoinCode)?.let { fromCoinType ->
        val dbModel = DbCryptoCoinModel().getCryptoCoin(Realm.getDefaultInstance(), fromCoinType.code())
        val toAddress = prefHelper.coinsFee[fromCoinType.code()]?.serverWalletAddress ?: ""
        val hashResponse =
            transactionHashRepository.createTransactionHash(fromCoinType, fromCoinAmount, dbModel, toAddress)
        return when {
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
    } ?: Either.Left(Failure.MessageError("Wrong coin type"))

    override suspend fun withdraw(
        smsCode: String,
        hash: String,
        coinFrom: String,
        coinFromAmount: Double
    ): Either<Failure, Unit> {
        val smsCodeVerifyResponse = apiService.verifySmsCode(smsCode)
        return if (smsCodeVerifyResponse.isRight) {
            apiService.withdraw(hash, coinFrom, coinFromAmount)
        } else {
            smsCodeVerifyResponse as Either.Left
        }
    }

    override suspend fun getGiftAddress(coinFrom: String, phone: String): Either<Failure, String> =
        apiService.getGiftAddress(coinFrom, phone)

    override suspend fun sendGift(
        smsCode: String,
        hash: String,
        coinFrom: String,
        coinFromAmount: Double,
        giftId: String,
        phone: String,
        message: String
    ): Either<Failure, Unit> {
        val smsCodeVerifyResponse = apiService.verifySmsCode(smsCode)
        return if (smsCodeVerifyResponse.isRight) {
            apiService.sendGift(hash, coinFrom, coinFromAmount, giftId, phone, message)
        } else {
            smsCodeVerifyResponse as Either.Left
        }
    }

    override suspend fun exchangeCoinToCoin(
        smsCode: String,
        coinFromAmount: Double,
        coinFrom: String,
        coinTo: String,
        hex: String
    ): Either<Failure, Unit> {
        val smsCodeVerifyResponse = apiService.verifySmsCode(smsCode)
        return if (smsCodeVerifyResponse.isRight) {
            apiService.coinToCoinExchange(coinFromAmount, coinFrom, coinTo, hex)
        } else {
            smsCodeVerifyResponse as Either.Left
        }
    }

}