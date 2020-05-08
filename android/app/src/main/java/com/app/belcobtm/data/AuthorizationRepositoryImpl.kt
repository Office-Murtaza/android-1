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
import com.app.belcobtm.domain.wallet.LocalCoinType
import org.web3j.utils.Numeric
import wallet.core.jni.*

class AuthorizationRepositoryImpl(
    private val prefHelper: SharedPreferencesHelper,
    private val apiService: AuthApiService,
    private val networkUtils: NetworkUtils,
    private val daoCoin: CoinDao
) : AuthorizationRepository {

    override suspend fun clearAppData(): Unit {
        prefHelper.accessToken = ""
        prefHelper.refreshToken = ""
        prefHelper.apiSeed = ""
        prefHelper.userPin = ""
        prefHelper.userId = -1
        daoCoin.clearTable()
        return Unit
    }

    override suspend fun recoverWallet(
        phone: String,
        password: String
    ): Either<Failure, Unit> = if (networkUtils.isNetworkAvailable()) {
        val response = apiService.recoverWallet(phone, password)

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
        val bitcoin = CoinType.BITCOIN
        val bitcoinCash = CoinType.BITCOINCASH
        val etherum = CoinType.ETHEREUM
        val litecoin = CoinType.LITECOIN
        val binance = CoinType.BINANCE
        val xrp = CoinType.XRP
        val tron = CoinType.TRON

        val wallet = HDWallet(128, "")

        val bitcoinPrivateKey = wallet.getKeyForCoin(bitcoin)
        val bitcoinPrivateKeyStr = Numeric.toHexStringNoPrefix(bitcoinPrivateKey.data())
        val extBitcoinPublicKey = wallet.getExtendedPublicKey(Purpose.BIP44, bitcoin, HDVersion.XPUB)
        val bitcoinPublicKey = HDWallet.getPublicKeyFromExtended(extBitcoinPublicKey, "m/44'/0'/0'/0/0")
        val bitcoinAddress = BitcoinAddress(bitcoinPublicKey, CoinType.BITCOIN.p2pkhPrefix()).description()
        val bitcoinChPrivateKey = wallet.getKeyForCoin(bitcoinCash)
        val bitcoinChPrivateKeyStr = Numeric.toHexStringNoPrefix(bitcoinChPrivateKey.data())
        val bitcoinChAddress = bitcoinCash.deriveAddress(bitcoinChPrivateKey)

        val etherumPrivateKey = wallet.getKeyForCoin(etherum)
        val etherumPrivateKeyStr = Numeric.toHexStringNoPrefix(etherumPrivateKey.data())
        val etherumAddress = etherum.deriveAddress(etherumPrivateKey)

        val litecoinPrivateKey = wallet.getKeyForCoin(litecoin)
        val litecoinPrivateKeyStr = Numeric.toHexStringNoPrefix(litecoinPrivateKey.data())
        val litecoinAddress = litecoin.deriveAddress(litecoinPrivateKey)

        val binancePrivateKey = wallet.getKeyForCoin(binance)
        val binancePrivateKeyStr = Numeric.toHexStringNoPrefix(binancePrivateKey.data())
        val binanceAddress = binance.deriveAddress(binancePrivateKey)

        val xrpPrivateKey = wallet.getKeyForCoin(xrp)
        val xrpPrivateKeyStr = Numeric.toHexStringNoPrefix(xrpPrivateKey.data())
        val xrpAddress = xrp.deriveAddress(xrpPrivateKey)

        val tronPrivateKey = wallet.getKeyForCoin(tron)
        val tronPrivateKeyStr = Numeric.toHexStringNoPrefix(tronPrivateKey.data())
        val tronAddress = tron.deriveAddress(tronPrivateKey)
        val entityList = listOf(
            CoinEntity(LocalCoinType.BTC, bitcoinAddress, bitcoinPrivateKeyStr),
            CoinEntity(LocalCoinType.BCH, bitcoinChAddress, bitcoinChPrivateKeyStr),
            CoinEntity(LocalCoinType.ETH, etherumAddress, etherumPrivateKeyStr),
            CoinEntity(LocalCoinType.LTC, litecoinAddress, litecoinPrivateKeyStr),
            CoinEntity(LocalCoinType.BNB, binanceAddress, binancePrivateKeyStr),
            CoinEntity(LocalCoinType.TRX, tronAddress, tronPrivateKeyStr),
            CoinEntity(LocalCoinType.XRP, xrpAddress, xrpPrivateKeyStr)
        )
        prefHelper.apiSeed = wallet.mnemonic()
        daoCoin.insertItemList(entityList)
        return entityList
    }
}