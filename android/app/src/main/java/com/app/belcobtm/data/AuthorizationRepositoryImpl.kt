package com.app.belcobtm.data

import com.app.belcobtm.data.core.NetworkUtils
import com.app.belcobtm.data.disk.database.CoinDao
import com.app.belcobtm.data.disk.database.CoinEntity
import com.app.belcobtm.data.disk.database.mapToDataItem
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.rest.authorization.AuthApiService
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.authorization.AuthorizationRepository
import com.app.belcobtm.domain.authorization.AuthorizationStatus
import com.app.belcobtm.domain.wallet.LocalCoinType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.web3j.utils.Numeric
import wallet.core.jni.*

class AuthorizationRepositoryImpl(
    private val prefHelper: SharedPreferencesHelper,
    private val apiService: AuthApiService,
    private val networkUtils: NetworkUtils,
    private val daoCoin: CoinDao
) : AuthorizationRepository {

    override fun getAuthorizationStatus(): AuthorizationStatus {
        val isEmptyAccountList: Boolean = runBlocking { daoCoin.isTableHasItems() } != null
        if (isEmptyAccountList && prefHelper.apiSeed.isNotEmpty()) {
            clearAppData()
        }
        return when {
            prefHelper.accessToken.isEmpty() -> AuthorizationStatus.UNAUTHORIZED //Welcome fragment
            prefHelper.apiSeed.isEmpty() -> AuthorizationStatus.SEED_PHRASE_ENTER//RecoverSeedActivity
            //AuthorizationStatus.SEED_PHRASE_CREATE
            prefHelper.userPin.isNotBlank() -> AuthorizationStatus.PIN_CODE_ENTER //PinActivity
            else -> AuthorizationStatus.PIN_CODE_CREATE
        }
    }

    override fun clearAppData() {
        prefHelper.accessToken = ""
        prefHelper.refreshToken = ""
        prefHelper.apiSeed = ""
        prefHelper.userPin = ""
        prefHelper.userId = -1
        CoroutineScope(Dispatchers.IO).launch { daoCoin.clearTable() }
    }

    override suspend fun recoverWallet(
        phone: String,
        password: String
    ): Either<Failure, Unit> = if (networkUtils.isNetworkAvailable()) {
        val response = apiService.checkCredentials(phone, password)

        if (response.isRight) {
            val body = (response as Either.Right).b
            when {
                !body.first -> Either.Left(Failure.IncorrectLogin)
                !body.second -> Either.Left(Failure.IncorrectPassword)
                else -> Either.Right(Unit)
            }
            //prefHelper.accessToken = body.accessToken
            //prefHelper.refreshToken = body.refreshToken
            //prefHelper.userId = body.userId
        } else {
            response as Either.Left
        }
    } else {
        Either.Left(Failure.NetworkConnection)
    }

    override suspend fun recoverWalletVerifySmsCode(
        smsCode: String
    ): Either<Failure, Unit> = if (networkUtils.isNetworkAvailable()) {
        apiService.recoverWalletVerifySmsCode(prefHelper.userId, smsCode)
    } else {
        Either.Left(Failure.NetworkConnection)
    }

    override suspend fun createWallet(phone: String, password: String): Either<Failure, Unit> =
        if (networkUtils.isNetworkAvailable()) {
            val response = apiService.registerWallet(phone, password)
            if (response.isRight) {
                val body = (response as Either.Right).b
                prefHelper.accessToken = body.accessToken
                prefHelper.refreshToken = body.refreshToken
                prefHelper.userId = body.userId
                Either.Right(Unit)
            } else {
                response as Either.Left
            }
        } else {
            Either.Left(Failure.NetworkConnection)
        }

    override suspend fun createWalletVerifySmsCode(
        smsCode: String
    ): Either<Failure, String> = if (networkUtils.isNetworkAvailable()) {
        val response = apiService.createWalletVerifySmsCode(prefHelper.userId, smsCode)
        if (response.isRight) {
            val coinList = createWalletDB()
            val coinListResponse = apiService.addCoins(prefHelper.userId, coinList.map { it.mapToDataItem() })
            if (coinListResponse.isRight) {
                Either.Right(prefHelper.apiSeed)
            } else {
                coinListResponse as Either.Left
            }
        } else {
            response as Either.Left
        }
    } else {
        Either.Left(Failure.NetworkConnection)
    }

    override suspend fun authorize(): Either<Failure, Unit> =
        if (networkUtils.isNetworkAvailable()) {
            val response = apiService.authorizeByRefreshToken(prefHelper.refreshToken)
            if (response.isRight) {
                val body = (response as Either.Right).b
                prefHelper.accessToken = body.accessToken
                prefHelper.refreshToken = body.refreshToken
                prefHelper.userId = body.userId
                Either.Right(Unit)
            } else {
                response as Either.Left
            }
        } else {
            Either.Left(Failure.NetworkConnection)
        }

    override fun getAuthorizePin(): String = prefHelper.userPin

    override fun setAuthorizePin(pinCode: String) {
        prefHelper.userPin = pinCode
    }

    private suspend fun createWalletDB(): List<CoinEntity> {
        val wallet = HDWallet(128, "")
        val entityList = LocalCoinType.values().map { createCoinEntity(it, wallet) }
        prefHelper.apiSeed = wallet.mnemonic()
        daoCoin.insertItemList(entityList)
        return entityList
    }

    private fun createCoinEntity(coinType: LocalCoinType, wallet: HDWallet): CoinEntity {
        val privateKey: PrivateKey = wallet.getKeyForCoin(coinType.trustWalletType)
        val address: String = when (coinType) {
            LocalCoinType.BTC -> {
                val extBitcoinPublicKey =
                    wallet.getExtendedPublicKey(Purpose.BIP44, coinType.trustWalletType, HDVersion.XPUB)
                val bitcoinPublicKey = HDWallet.getPublicKeyFromExtended(extBitcoinPublicKey, "m/44'/0'/0'/0/0")
                BitcoinAddress(bitcoinPublicKey, coinType.trustWalletType.p2pkhPrefix()).description()
            }
            else -> coinType.trustWalletType.deriveAddress(privateKey)
        }
        return CoinEntity(coinType, address, Numeric.toHexStringNoPrefix(privateKey.data()))
    }
}