package com.app.belcobtm.data

import com.app.belcobtm.data.rest.wallet.WalletApiService
import com.app.belcobtm.data.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.CoinFeeDataItem
import com.app.belcobtm.domain.wallet.WalletRepository

class WalletRepositoryImpl(
    private val apiService: WalletApiService,
    private val prefHelper: SharedPreferencesHelper
) : WalletRepository {
    override fun getCoinFeeMap(): Map<String, CoinFeeDataItem> = prefHelper.coinsFee

    override suspend fun exchangeCoinToCoin(
        coinFromAmount: Double,
        coinFrom: String,
        coinTo: String,
        hex: String
    ): Either<Failure, Unit> = apiService.coinToCoinExchange(coinFromAmount, coinFrom, coinTo, hex)

    override suspend fun sendToDeviceSmsCode(): Either<Failure, Unit> = apiService.sendToDeviceSmsCode()

    override suspend fun verifySmsCode(smsCode: String): Either<Failure, Unit> = apiService.verifySmsCode(smsCode)
}