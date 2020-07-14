package com.app.belcobtm.data

import com.app.belcobtm.data.core.NetworkUtils
import com.app.belcobtm.data.disk.database.AccountDao
import com.app.belcobtm.data.disk.database.AccountEntity
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.rest.authorization.AuthApiService
import com.app.belcobtm.data.rest.authorization.response.RecoverWalletCoinResponse
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
    private val daoAccount: AccountDao
) : AuthorizationRepository {

    override fun getAuthorizationStatus(): AuthorizationStatus {
        val isEmptyAccountList: Boolean = runBlocking { daoAccount.isTableHasItems() } != null
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
        CoroutineScope(Dispatchers.IO).launch { daoAccount.clearTable() }
    }

    override suspend fun authorizationCheckCredentials(
        phone: String,
        password: String
    ): Either<Failure, Boolean> = if (networkUtils.isNetworkAvailable()) {
        val response = apiService.recoverWalletCheckCredentials(phone, password)

        if (response.isRight) {
            val body = (response as Either.Right).b
            when {
                !body.second -> Either.Left(Failure.IncorrectPassword)
                else -> Either.Right(body.first)
            }
        } else {
            response as Either.Left
        }
    } else {
        Either.Left(Failure.NetworkConnection)
    }

    override suspend fun authorizationVerifySmsCode(
        smsCode: String
    ): Either<Failure, Unit> = if (networkUtils.isNetworkAvailable()) {
        apiService.recoverWalletVerifySmsCode(prefHelper.userId, smsCode)
    } else {
        Either.Left(Failure.NetworkConnection)
    }

    private fun createAccountEntityList(
        temporaryCoinMap: Map<LocalCoinType, Pair<String, String>>,
        responseCoinList: List<RecoverWalletCoinResponse>
    ): List<AccountEntity> {
        val entityList: MutableList<AccountEntity> = mutableListOf()
        temporaryCoinMap.forEach { (localCoinType, value) ->
            val publicKey: String = value.first
            val privateKey: String = value.second
            responseCoinList.find { it.code == localCoinType.name }?.let { responseItem ->
                entityList.add(AccountEntity(responseItem.id, localCoinType, publicKey, privateKey, true))
            }
        }
        return entityList
    }

    override suspend fun recoverWallet(
        seed: String,
        phone: String,
        password: String
    ): Either<Failure, Unit> = if (networkUtils.isNetworkAvailable()) {
        val wallet = HDWallet(seed, "")
        val temporaryCoinMap = LocalCoinType.values().map { Pair(it, createTemporaryCoin(it, wallet)) }.toMap()
        val recoverResponse =
            apiService.recoverWallet(phone, password, temporaryCoinMap.map { it.key.name to it.value.first }.toMap())

        if (recoverResponse.isRight) {
            val result = (recoverResponse as Either.Right).b
            val accountList = createAccountEntityList(temporaryCoinMap, result.balance.coins)
            daoAccount.insertItemList(accountList)
            prefHelper.apiSeed = seed
            prefHelper.accessToken = result.accessToken
            prefHelper.refreshToken = result.refreshToken
            prefHelper.userId = result.userId
        }
        Either.Right(Unit)
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
//        val response = apiService.createWalletVerifySmsCode(prefHelper.userId, smsCode)
//        if (response.isRight) {
//            val coinList = createWalletDB()
//            val coinListResponse = apiService.addCoins(prefHelper.userId, coinList.map { it.mapToDataItem() })
//            if (coinListResponse.isRight) {
//                Either.Right(prefHelper.apiSeed)
//            } else {
//                coinListResponse as Either.Left
//            }
//        } else {
//            response as Either.Left
//        }
        Either.Left(Failure.MessageError("deprecated"))
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

//    private suspend fun createWalletDB(): List<AccountEntity> {
//        val wallet = HDWallet(128, "")
//        val entityList = LocalCoinType.values().map { createCoinEntity(it, wallet) }
//        prefHelper.apiSeed = wallet.mnemonic()
//        daoAccount.insertItemList(entityList)
//        return entityList
//    }

//    private fun createCoinEntity(coinType: LocalCoinType, wallet: HDWallet): AccountEntity {
//        val privateKey: PrivateKey = wallet.getKeyForCoin(coinType.trustWalletType)
//        val address: String = when (coinType) {
//            LocalCoinType.BTC -> {
//                val extBitcoinPublicKey =
//                    wallet.getExtendedPublicKey(Purpose.BIP44, coinType.trustWalletType, HDVersion.XPUB)
//                val bitcoinPublicKey = HDWallet.getPublicKeyFromExtended(extBitcoinPublicKey, "m/44'/0'/0'/0/0")
//                BitcoinAddress(bitcoinPublicKey, coinType.trustWalletType.p2pkhPrefix()).description()
//            }
//            else -> coinType.trustWalletType.deriveAddress(privateKey)
//        }
//        return AccountEntity(coinType, address, Numeric.toHexStringNoPrefix(privateKey.data()))
//    }

    private fun createTemporaryCoin(coinType: LocalCoinType, wallet: HDWallet): Pair<String, String> {
        val privateKey: PrivateKey = wallet.getKeyForCoin(coinType.trustWalletType)
        val publicKey: String = when (coinType) {
            LocalCoinType.BTC -> {
                val extBitcoinPublicKey =
                    wallet.getExtendedPublicKey(Purpose.BIP44, coinType.trustWalletType, HDVersion.XPUB)
                val bitcoinPublicKey = HDWallet.getPublicKeyFromExtended(extBitcoinPublicKey, "m/44'/0'/0'/0/0")
                BitcoinAddress(bitcoinPublicKey, coinType.trustWalletType.p2pkhPrefix()).description()
            }
            else -> coinType.trustWalletType.deriveAddress(privateKey)
        }
        return Pair(publicKey, Numeric.toHexStringNoPrefix(privateKey.data()))
    }
}