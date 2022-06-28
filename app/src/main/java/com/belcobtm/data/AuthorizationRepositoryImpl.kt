package com.belcobtm.data

import com.belcobtm.data.disk.database.account.AccountDao
import com.belcobtm.data.disk.database.account.AccountEntity
import com.belcobtm.data.disk.database.wallet.WalletDao
import com.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.belcobtm.data.provider.location.LocationProvider
import com.belcobtm.data.rest.authorization.AuthApiService
import com.belcobtm.data.rest.authorization.response.CreateRecoverWalletResponse
import com.belcobtm.data.rest.wallet.response.BalanceResponse
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.authorization.AuthorizationRepository
import com.belcobtm.domain.authorization.AuthorizationStatus
import com.belcobtm.domain.service.ServiceRepository
import com.belcobtm.domain.settings.type.VerificationStatus
import com.belcobtm.domain.wallet.LocalCoinType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.web3j.utils.Numeric
import wallet.core.jni.BitcoinAddress
import wallet.core.jni.HDVersion
import wallet.core.jni.HDWallet
import wallet.core.jni.PrivateKey
import wallet.core.jni.Purpose
import java.text.SimpleDateFormat
import java.util.Locale

class AuthorizationRepositoryImpl(
    private val prefHelper: SharedPreferencesHelper,
    private val apiService: AuthApiService,
    private val daoAccount: AccountDao,
    private val walletDao: WalletDao,
    private val serviceRepository: ServiceRepository,
    private val locationProvider: LocationProvider
) : AuthorizationRepository {

    private val temporaryCoinMap: MutableMap<LocalCoinType, Pair<String, String>> by lazy {
        return@lazy mutableMapOf<LocalCoinType, Pair<String, String>>()
    }

    override fun getAuthorizationStatus(): AuthorizationStatus {
        if (!prefHelper.isUserAuthed && prefHelper.apiSeed.isNotEmpty()) {
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
        CoroutineScope(Dispatchers.IO).launch {
            daoAccount.clearTable()
            walletDao.clear()
        }
    }

    override fun getVerificationStatus(): Either<Failure, VerificationStatus> {
        val status = VerificationStatus.values().find { it.stringValue == prefHelper.userStatus }
        return if (status != null) {
            Either.Right(status)
        } else {
            Either.Left(Failure.NetworkConnection)
        }
    }

    override suspend fun authorizationCheckCredentials(
        phone: String,
        password: String,
        email: String
    ): Either<Failure, Triple<Boolean, Boolean, Boolean>> {
        val response = apiService.authorizationCheckCredentials(phone, password, email)
        return if (response.isRight) {
            val body = (response as Either.Right).b
            Either.Right(body)
        } else {
            response as Either.Left
        }
    }

    override suspend fun createSeedPhrase(): Either<Failure, String> {
        val wallet = HDWallet(128, "")
        temporaryCoinMap.clear()
        temporaryCoinMap.putAll(
            LocalCoinType.values().associate { Pair(it, createTemporaryAccount(it, wallet)) }
        )
        prefHelper.apiSeed = wallet.mnemonic()
        return Either.Right(prefHelper.apiSeed)
    }

    override suspend fun saveSeed(seed: String): Either<Failure, Unit> {
        val formattedSeed = seed.trim()
        val wallet = HDWallet(formattedSeed, "")
        temporaryCoinMap.clear()
        temporaryCoinMap.putAll(
            LocalCoinType.values().associate { Pair(it, createTemporaryAccount(it, wallet)) }
        )
        prefHelper.apiSeed = formattedSeed
        return Either.Right(Unit)
    }

    override suspend fun createWallet(
        phone: String,
        password: String,
        email: String,
        notificationToken: String
    ): Either<Failure, Unit> {
        val location = locationProvider.getCurrentLocation()
        return apiService.createWallet(
            phone = phone,
            password = password,
            email = email,
            timezone = getDeviceTimezone(),
            lat = location?.latitude,
            lng = location?.longitude,
            notificationToken = notificationToken,
            coinMap = temporaryCoinMap.map { it.key.name to it.value.first }.toMap()
        ).let {
            handleCreateRecoverResponse(it, phone)
        }
    }

    override suspend fun recoverWallet(
        seed: String,
        phone: String,
        password: String,
        notificationToken: String
    ): Either<Failure, Unit> {
        val location = locationProvider.getCurrentLocation()
        val wallet = HDWallet(seed, "")
        temporaryCoinMap.clear()
        temporaryCoinMap.putAll(
            LocalCoinType.values().associate { Pair(it, createTemporaryAccount(it, wallet)) }
        )
        return apiService.recoverWallet(
            phone,
            password,
            location?.latitude,
            location?.longitude,
            getDeviceTimezone(),
            notificationToken,
            temporaryCoinMap.map { it.key.name to it.value.first }.toMap()
        ).let {
            prefHelper.apiSeed = seed
            handleCreateRecoverResponse(it, phone)
        }
    }

    private suspend fun handleCreateRecoverResponse(
        response: Either<Failure, CreateRecoverWalletResponse>,
        phone: String,
    ) = response.takeIf { it.isRight }?.let {
        with((response as Either.Right).b) {
            val accountList = createAccountEntityList(temporaryCoinMap, balance)
            serviceRepository.updateServices(services)
            walletDao.updateBalance(balance)
            daoAccount.insertItemList(accountList)
            prefHelper.firebaseToken = firebaseToken
            prefHelper.accessToken = accessToken
            prefHelper.refreshToken = refreshToken
            prefHelper.userId = user.id
            prefHelper.userStatus = user.status
            prefHelper.referralCode = user.referralCode.orEmpty()
            prefHelper.referralInvites = user.referrals ?: 0
            prefHelper.referralEarned = user.referralEarned ?: 0.0
            prefHelper.zendeskToken = zendeskToken
            prefHelper.userPhone = phone
            prefHelper.userFirstName = user.firstName.orEmpty()
            prefHelper.userLastName = user.lastName.orEmpty()
            temporaryCoinMap.clear()
            Either.Right(Unit)
        }
    } ?: response as Either.Left

    override suspend fun authorize(): Either<Failure, Unit> {
        val response = apiService.authorizeByRefreshToken(prefHelper.refreshToken)
        return response.takeIf { it.isRight }?.let {
            val body = (response as Either.Right).b
            walletDao.updateBalance(body.balance)
            prefHelper.processAuthResponse(body)
            Either.Right(Unit)
        } ?: response as Either.Left
    }

    override fun getAuthorizePin(): String = prefHelper.userPin

    override fun setAuthorizePin(pinCode: String) {
        prefHelper.userPin = pinCode
    }

    override fun setIsUserAuthed(isAuthed: Boolean) {
        prefHelper.isUserAuthed = isAuthed
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
        balance: BalanceResponse
    ): List<AccountEntity> {
        val entityList: MutableList<AccountEntity> = mutableListOf()
        val balanceCoins = balance.coins.map { it.coin }
        temporaryCoinMap.forEach { (localCoinType, value) ->
            val publicKey: String = value.first
            val privateKey: String = value.second
            balance.availableCoins.find { it == localCoinType.name }?.let {
                entityList.add(
                    AccountEntity(
                        localCoinType.name,
                        publicKey,
                        privateKey,
                        balanceCoins.contains(it)
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

    /**
     * Should return formatted GMT timezone
     * e.g GMT+02:00
     * */
    private fun getDeviceTimezone(): String {
        val pattern = "ZZZZ"
        val currentTimeMs = System.currentTimeMillis()
        return SimpleDateFormat(pattern, Locale.getDefault())
            .format(currentTimeMs)
    }

}
