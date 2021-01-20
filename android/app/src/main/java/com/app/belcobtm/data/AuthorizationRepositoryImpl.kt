package com.app.belcobtm.data

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
    private val daoAccount: AccountDao
) : AuthorizationRepository {
    private val temporaryCoinMap: MutableMap<LocalCoinType, Pair<String, String>> by lazy {
        return@lazy mutableMapOf<LocalCoinType, Pair<String, String>>()
    }

    override fun getAuthorizationStatus(): AuthorizationStatus {
        val isEmptyAccountList = runBlocking { daoAccount.isTableHasItems() } == null
        if (isEmptyAccountList && prefHelper.apiSeed.isNotEmpty()) {
            clearAppData()
        }
        return when {
            prefHelper.refreshToken.isEmpty() -> AuthorizationStatus.UNAUTHORIZED //Welcome fragment
            prefHelper.userPin.isNotBlank() -> AuthorizationStatus.PIN_CODE_ENTER //PinActivity
            else -> AuthorizationStatus.PIN_CODE_CREATE
        }
    }

    override fun clearAppData() {
        prefHelper.clearData()
        CoroutineScope(Dispatchers.IO).launch { daoAccount.clearTable() }
    }

    override suspend fun authorizationCheckCredentials(
        phone: String,
        password: String
    ): Either<Failure, Pair<Boolean, Boolean>> {
        val response = apiService.authorizationCheckCredentials(phone, password)
        return if (response.isRight) {
            val body = (response as Either.Right).b
            Either.Right(Pair(body.first, body.second))
        } else {
            response as Either.Left
        }
    }

    override suspend fun createSeedPhrase(): Either<Failure, String> {
        val wallet = HDWallet(128, "")
        temporaryCoinMap.clear()
        temporaryCoinMap.putAll(
            LocalCoinType.values().map { Pair(it, createTemporaryAccount(it, wallet)) }.toMap()
        )
        prefHelper.apiSeed = wallet.mnemonic()
        return Either.Right(prefHelper.apiSeed)
    }

    override suspend fun saveSeed(seed: String): Either<Failure, Unit> {
        val wallet = HDWallet(128, seed)
        temporaryCoinMap.clear()
        temporaryCoinMap.putAll(
            LocalCoinType.values().map { Pair(it, createTemporaryAccount(it, wallet)) }.toMap()
        )
        prefHelper.apiSeed = seed
        return Either.Right(Unit)
    }

    override suspend fun createWallet(
        phone: String,
        password: String,
        notificationToken: String
    ): Either<Failure, Unit> {
        val response = apiService.createWallet(
            phone = phone,
            password = password,
            notificationToken = notificationToken,
            coinMap = temporaryCoinMap.map { it.key.name to it.value.first }.toMap()
        )

        return if (response.isRight) {
            val result = (response as Either.Right).b
            val accountList = createAccountEntityList(temporaryCoinMap, result.balance.coins)
            daoAccount.insertItemList(accountList)
            prefHelper.accessToken = result.accessToken
            prefHelper.refreshToken = result.refreshToken
            prefHelper.userId = result.userId
            temporaryCoinMap.clear()
            Either.Right(Unit)
        } else {
            response as Either.Left
        }
    }

    override suspend fun recoverWallet(
        seed: String,
        phone: String,
        password: String,
        notificationToken: String
    ): Either<Failure, Unit> {
        val wallet = HDWallet(seed, "")
        temporaryCoinMap.clear()
        temporaryCoinMap.putAll(
            LocalCoinType.values().map { Pair(it, createTemporaryAccount(it, wallet)) }.toMap()
        )
        val recoverResponse =
            apiService.recoverWallet(
                phone, password, notificationToken,
                temporaryCoinMap.map { it.key.name to it.value.first }.toMap()
            )

        return if (recoverResponse.isRight) {
            val result = (recoverResponse as Either.Right).b
            val accountList = createAccountEntityList(temporaryCoinMap, result.balance.coins)
            daoAccount.insertItemList(accountList)
            prefHelper.apiSeed = seed
            prefHelper.accessToken = result.accessToken
            prefHelper.refreshToken = result.refreshToken
            prefHelper.userId = result.userId
            temporaryCoinMap.clear()
            Either.Right(Unit)
        } else {
            recoverResponse as Either.Left
        }
    }

    override suspend fun authorize(): Either<Failure, Unit> {
        val response = apiService.authorizeByRefreshToken(prefHelper.refreshToken)
        return if (response.isRight) {
            val body = (response as Either.Right).b
            prefHelper.processAuthResponse(body)
            Either.Right(Unit)
        } else {
            response as Either.Left
        }
    }

    override fun getAuthorizePin(): String = prefHelper.userPin

    override fun setAuthorizePin(pinCode: String) {
        prefHelper.userPin = pinCode
    }

    private fun createTemporaryAccount(
        coinType: LocalCoinType,
        wallet: HDWallet
    ): Pair<String, String> {
        val privateKey: PrivateKey = wallet.getKeyForCoin(coinType.trustWalletType)
        val publicKey: String = when (coinType) {
            LocalCoinType.BTC -> {
                val extBitcoinPublicKey =
                    wallet.getExtendedPublicKey(
                        Purpose.BIP44,
                        coinType.trustWalletType,
                        HDVersion.XPUB
                    )
                val bitcoinPublicKey =
                    HDWallet.getPublicKeyFromExtended(
                        extBitcoinPublicKey,
                        coinType.trustWalletType,
                        "m/44'/0'/0'/0/0"
                    )
                BitcoinAddress(
                    bitcoinPublicKey,
                    coinType.trustWalletType.p2pkhPrefix()
                ).description()
            }
            else -> coinType.trustWalletType.deriveAddress(privateKey)
        }
        return Pair(publicKey, Numeric.toHexStringNoPrefix(privateKey.data()))
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
                entityList.add(
                    AccountEntity(
                        responseItem.idx,
                        localCoinType,
                        publicKey,
                        privateKey,
                        true
                    )
                )
            }
        }
        return entityList
    }

    override suspend fun checkPass(userId: String, password: String): Either<Failure, Boolean> {
        val response = apiService.checkPass(userId, password)
        return if (response.isRight) {
            Either.Right((response as Either.Right).b.result)
        } else {
            response as Either.Left
        }
    }
}