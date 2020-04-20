package com.app.belcobtm.presentation.core.helper

import android.preference.PreferenceManager
import com.app.belcobtm.App
import com.app.belcobtm.api.data_manager.WithdrawDataManager
import com.app.belcobtm.api.model.response.BNBBlockResponse
import com.app.belcobtm.api.model.response.ETHResponse
import com.app.belcobtm.api.model.response.TronBlockResponse
import com.app.belcobtm.api.model.response.UtxoItem
import com.app.belcobtm.data.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.db.DbCryptoCoin
import com.app.belcobtm.presentation.core.*
import com.app.belcobtm.presentation.core.Optional
import com.app.belcobtm.presentation.core.extensions.*
import com.google.protobuf.ByteString
import io.reactivex.Observable
import wallet.core.jni.*
import wallet.core.jni.proto.*
import java.math.BigDecimal
import java.util.*

class CryptoHashHelper {
    private val dataManager: WithdrawDataManager = WithdrawDataManager()

    //TODO need migrate to dependency koin after refactoring
    private val prefsHelper: SharedPreferencesHelper by lazy {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.appContext())
        SharedPreferencesHelper(sharedPreferences)
    }

    fun getCoinTransactionHashObs(
        hdWallet: HDWallet,
        toAddress: String,
        coinType: CoinType?,
        coinAmount: Double,
        mCoinDbModel: DbCryptoCoin?
    ): Observable<String> {
        val mUserId = prefsHelper.userId.toString()
        return when (coinType) {
            CoinType.XRP -> getXRPTransactionHashObs(
                toAddress,
                coinAmount,
                mCoinDbModel,
                mUserId
            )
            CoinType.BINANCE -> getBNBTransactionHashObs(
                toAddress,
                coinAmount,
                mUserId,
                mCoinDbModel
            )
            CoinType.BITCOIN,
            CoinType.BITCOINCASH,
            CoinType.LITECOIN -> getBTCTransactionHashObs(
                hdWallet,
                toAddress,
                coinType,
                coinAmount,
                mUserId,
                mCoinDbModel
            )
            CoinType.TRON -> getTronTransactionHashObs(
                toAddress,
                coinAmount,
                mUserId,
                mCoinDbModel
            )
            CoinType.ETHEREUM -> getETHTransactionHashObs(
                toAddress,
                coinAmount,
                mUserId,
                mCoinDbModel
            )
            else -> Observable.just("")
        }
    }

    private fun getXRPTransactionHashObs(
        toAddress: String,
        coinAmount: Double,
        mCoinDbModel: DbCryptoCoin?,
        mUserId: String
    ): Observable<String> {
        val cryptoToSubCoin = coinAmount * CoinType.XRP.unit()
        val fromAddress = mCoinDbModel?.publicKey ?: ""
        val privateKey = PrivateKey(mCoinDbModel?.privateKey?.toHexByteArray())
        return dataManager.getXRPBlockHeader(mUserId, fromAddress).map { resp ->
            createXRPTransaction(fromAddress, cryptoToSubCoin, toAddress, privateKey, resp)
        }
    }

    private fun createXRPTransaction(
        fromAddress: String?,
        cryptoToSubcoin: Double,
        toAddress: String,
        privateKey_: PrivateKey,
        resp: Optional<BNBBlockResponse>
    ): String {
        val signingInput = Ripple.SigningInput.newBuilder()
        val respFee = (prefsHelper.coinsFee[CoinType.XRP.code()]?.txFee?.toBigDecimal()
            ?: BigDecimal(0.000020)) * BigDecimal.valueOf(CoinType.XRP.unit())
        val respSeq = resp.value?.sequence?.toInt() ?: 0

        signingInput.apply {
            account = fromAddress
            amount = cryptoToSubcoin.toLong()
            destination = toAddress
            fee = (respFee).toLong()

            sequence = respSeq
            privateKey = ByteString.copyFrom(privateKey_.data())
        }

        val sign: Ripple.SigningOutput = RippleSigner.sign(signingInput.build())
        val signBytes = sign.encoded.toByteArray()
        return Numeric.toHexString(signBytes)
    }

    private fun getTronTransactionHashObs(
        toAddress: String,
        coinAmount: Double,
        mUserId: String?,
        mCoinDbModel: DbCryptoCoin?
    ): Observable<String> = dataManager.getTronBlockHeader(mUserId, mCoinDbModel!!.coinType).map { resp ->
        createTronTransactionHash(
            toAddress,
            mCoinDbModel,
            resp.value,
            coinAmount
        )
    }

    private fun createTronTransactionHash(
        toAddress: String, mCoinDbModel: DbCryptoCoin?, resp: TronBlockResponse?, coinAmount: Double
    ): String? {
        val cryptoToSubcoin = coinAmount * CoinType.TRON.unit()
        val fromAddress = mCoinDbModel?.publicKey
        val tronBlock = Tron.BlockHeader.newBuilder()

        tronBlock.number = resp?.blockHeader?.raw_data?.number ?: 0L
        tronBlock.timestamp = resp?.blockHeader?.raw_data?.timestamp ?: 0L
        tronBlock.version = resp?.blockHeader?.raw_data?.version ?: 0
        tronBlock.parentHash = resp?.blockHeader?.raw_data?.parentHash?.toHexBytesInByteString()
        tronBlock.witnessAddress =
            resp?.blockHeader?.raw_data?.witnessAddress?.toHexBytesInByteString()
        tronBlock.txTrieRoot = resp?.blockHeader?.raw_data?.txTrieRoot?.toHexBytesInByteString()

        val transferBuilder = Tron.TransferContract.newBuilder()

        transferBuilder.ownerAddress = fromAddress
        transferBuilder.toAddress = toAddress
        transferBuilder.amount = cryptoToSubcoin.toLong()

        val transaction = Tron.Transaction.newBuilder()
        transaction.transfer = transferBuilder.build()

        transaction.timestamp = Date().time

        val cal = Calendar.getInstance()
        cal.time = Date()
        cal.add(Calendar.HOUR, 10)
        val resHour = cal.time

        transaction.expiration = resHour.time
        transaction.feeLimit = ((prefsHelper.coinsFee[CoinType.TRON.code()]?.txFee?.toBigDecimal()
            ?: BigDecimal(1)) * BigDecimal.valueOf(CoinType.TRON.unit())).toLong()
        transaction.blockHeader = tronBlock.build()

        val signing = Tron.SigningInput.newBuilder()

        signing.transaction = transaction.build()
        signing.privateKey = mCoinDbModel?.privateKey?.toHexBytesInByteString()

        val output: Tron.SigningOutput = TronSigner.sign(signing.build())
        return output.json
    }

    private fun getETHTransactionHashObs(
        toAddress: String,
        coinAmount: Double,
        mUserId: String,
        mCoinDbModel: DbCryptoCoin?
    ): Observable<String> = dataManager.getETHNonce(mUserId, toAddress).map { resp ->
        createETHTransactionHash(
            toAddress,
            mCoinDbModel!!,
            resp.value,
            coinAmount
        )
    }

    /**
     * custom implementation of adding leading zeroes
     * for hex value (%016llx)
     */
    private fun addLeadingZeroes(str: String): String? {
        var res = ""
        if (str.length < 64) {
            var i = 0
            while ((64 - str.length) > i) {
                i++
                res += "0"
            }
            return res + str
        }
        return str
    }

    private fun createETHTransactionHash(
        _toAddress: String,
        mCoinDbModel: DbCryptoCoin,
        resp: ETHResponse?,
        coinAmount: Double
    ): String? {
        val cryptoToSubcoin = BigDecimal(coinAmount * CoinType.ETHEREUM.unit())
        val nonsStr: String = resp?.nonce?.toString(16) ?: ""
        val nonceHex = ByteString.copyFrom("0x${addLeadingZeroes(nonsStr)}".toHexByteArray())
        val amountHex =
            ByteString.copyFrom("0x${addLeadingZeroes(cryptoToSubcoin.toLong().toString(16))}".toHexByteArray())
        val gasPriceD = prefsHelper.coinsFee[CoinType.ETHEREUM.code()]?.gasPrice?.toLong() ?: 20_000_000_000
        val gasLimitD = prefsHelper.coinsFee[CoinType.ETHEREUM.code()]?.gasLimit?.toLong() ?: 21000
        val gasLimitHex =
            ByteString.copyFrom("0x${addLeadingZeroes(gasLimitD.toString(16))}".toHexByteArray())
        val gasPriceHex =
            ByteString.copyFrom("0x${addLeadingZeroes(gasPriceD.toString(16))}".toHexByteArray())
        val signingInput = Ethereum.SigningInput.newBuilder()

        signingInput.apply {
            privateKey = mCoinDbModel.privateKey.toHexBytesInByteString()
            toAddress = _toAddress
            chainId = ByteString.copyFrom("0x1".toHexByteArray())
            nonce = nonceHex
            gasPrice = gasPriceHex
            gasLimit = gasLimitHex
            amount = amountHex
        }

        val sign: Ethereum.SigningOutput = EthereumSigner.sign(signingInput.build())
        val resTransactionHashStr = Numeric.toHexString(sign.encoded.toByteArray())
        return "0x$resTransactionHashStr"
    }


    private fun getBNBTransactionHashObs(
        toAddress: String,
        coinAmount: Double,
        mUserId: String,
        mCoinDbModel: DbCryptoCoin?
    ): Observable<String> {
        val cryptoToSubcoin = coinAmount * CoinType.BINANCE.unit()
        val privateKey = PrivateKey(mCoinDbModel?.privateKey?.toHexByteArray())
        val publicKey = privateKey.getPublicKeySecp256k1(true)

        return dataManager.getBNBBlockHeader(mUserId, mCoinDbModel?.publicKey ?: "").map { resp ->
            createBNBTransactionHash(
                toAddress,
                resp.value,
                cryptoToSubcoin,
                privateKey, publicKey
            )
        }
    }

    private fun createBNBTransactionHash(
        toAddress: String,
        resp: BNBBlockResponse?,
        cryptoToSubcoin: Double,
        privateKey: PrivateKey,
        publicKey: PublicKey?
    ): String? {
        val signingInput = Binance.SigningInput.newBuilder()

        signingInput.chainId = "Binance-Chain-Tigris"
        signingInput.accountNumber = resp?.accountNumber ?: 0
        signingInput.sequence = resp?.sequence ?: 0
        signingInput.privateKey = ByteString.copyFrom(privateKey.data())

        val token = Binance.SendOrder.Token.newBuilder()
        token.denom = CoinType.BINANCE.code()
        token.amount = cryptoToSubcoin.toLong()

        val input = Binance.SendOrder.Input.newBuilder()
        input.address = ByteString.copyFrom(CosmosAddress(HRP.BINANCE, publicKey).keyHash())
        input.addAllCoins(listOf(token.build()))

        val output = Binance.SendOrder.Output.newBuilder()
        output.address = ByteString.copyFrom(CosmosAddress(toAddress).keyHash())
        output.addAllCoins(listOf(token.build()))

        val sendOrder = Binance.SendOrder.newBuilder()
        sendOrder.addAllInputs(listOf(input.build()))
        sendOrder.addAllOutputs(listOf(output.build()))

        signingInput.sendOrder = sendOrder.build()

        val sign: Binance.SigningOutput = BinanceSigner.sign(signingInput.build())
        val signBytes = sign.encoded.toByteArray()

        return Numeric.toHexString(signBytes)
    }

    private fun getBTCTransactionHashObs(
        hdWallet: HDWallet,
        toAddress: String,
        coinType: CoinType,
        coinAmount: Double,
        mUserId: String,
        mCoinDbModel: DbCryptoCoin?
    ): Observable<String> {
        val extendedPublicKey = hdWallet.getExtendedPublicKey(
            coinType.customPurpose(),
            coinType,
            coinType.customXpubVersion()
        )

        return dataManager.getBTCUtxos(mUserId, mCoinDbModel!!.coinType, extendedPublicKey).map { utxosResponse ->
            createBTCTransactionHash(
                toAddress,
                mCoinDbModel,
                coinType,
                utxosResponse.value!!.utxoList,
                coinAmount
            )
        }
    }

    private fun createBTCTransactionHash(
        toAddress: String,
        coinDbModel: DbCryptoCoin?,
        coinType: CoinType,
        utxos: ArrayList<UtxoItem>,
        coinAmount: Double
    ): String {
        val hdWallet = HDWallet(prefsHelper.apiSeed, "")
        val publicKeyFrom = coinDbModel?.publicKey
        val cryptoToSatoshi = coinAmount * CoinType.BITCOIN.unit()
        val amount: Long = cryptoToSatoshi.toLong()
        val byteFee = getByteFee(coinDbModel?.coinType)
        val sngHash = TWBitcoinSigHashType.getCryptoHash(coinType)
//        val cointypeValue = if (coinType.value() == 2) 0 else coinType.value()
        val cointypeValue = coinType.value()
        val signerBuilder = Bitcoin.SigningInput.newBuilder()
            .setAmount(amount)
            .setHashType(sngHash)
            .setToAddress(toAddress)
            .setChangeAddress(publicKeyFrom)
            .setByteFee(byteFee)
            .setCoinType(cointypeValue)

        utxos.forEach {
            val privateKey = hdWallet.getKey(it.path)
            signerBuilder.addPrivateKey(ByteString.copyFrom(privateKey.data()))
        }

        utxos.forEach {
            val redeemScript = BitcoinScript.buildForAddress(it.address, coinType)
            val keyHash = if (redeemScript.isPayToWitnessScriptHash) {
                redeemScript.matchPayToWitnessPublicKeyHash()
            } else {
                redeemScript.matchPayToPubkeyHash()
            }

            if (keyHash.isNotEmpty()) {
                val key = Numeric.toHexString(keyHash)
                val scriptByteString = ByteString.copyFrom(redeemScript.data())
                signerBuilder.putScripts(key, scriptByteString)
            }
        }

        utxos.forEachIndexed { currentIndex, utxo ->
            val hash = utxo.txid.toHexBytes()
            val reversedHash = hash.reversed().toByteArray()
            val reversedHashStr = ByteString.copyFrom(reversedHash)
            val index = utxo.vout
            val sequence = Int.MAX_VALUE - utxos.size + currentIndex
            val outpoint = Bitcoin.OutPoint.newBuilder()
                .setHash(reversedHashStr)
                .setIndex(index)
                .setSequence(sequence)
                .build()
            val utxoAmount = utxo.value.toLong()
            val redeemScript = BitcoinScript.buildForAddress(utxo.address, coinType)
            val scriptByteString = ByteString.copyFrom(redeemScript.data())
            val utxo0 = Bitcoin.UnspentTransaction.newBuilder()
                .setScript(scriptByteString)
                .setAmount(utxoAmount)
                .setOutPoint(outpoint)
                .build()

            signerBuilder.addUtxo(utxo0)
        }

        val signer = BitcoinTransactionSigner(signerBuilder.build())
        val result = signer.sign()
        val output = result.getObjects(0).unpack(Bitcoin.SigningOutput::class.java)
        return Numeric.toHexString(output.encoded.toByteArray())
    }

    fun validateAddress(coinId: String, walletAddress: String): Boolean =
        CoinTypeExtension.getTypeByCode(coinId)?.validate(walletAddress) ?: false

    /**
     *
    bitcoin: 0.0004
    bitcoinCash: 0.0004
    litecoin: 0.00004
    ethereum: 0.00042
    binance: 0.001
    tron: 1
    ripple: 0.00002

    Server
    BTC  Bitcoin  0.0000004000
    BCH  Bitcoin Cash  0.0000004000
    ETH  Ethereum
    LTC  Litecoin  0.0000000400

    XRP  Ripple  0.0000200000
    TRX  Tron  1.0000000000
    BNB  Binance Coin  0.0010000000

     */
    fun getTransactionFee(coinName: String): Double = prefsHelper.coinsFee[coinName]?.txFee ?: 0.0

    private fun getByteFee(coinName: String?): Long {
        val coinTypeUnit: Long = CoinTypeExtension.getTypeByCode(coinName ?: "")?.unit() ?: 0
        val byteFee = prefsHelper.coinsFee[coinName]?.byteFee ?: Double.MIN_VALUE
        return (byteFee * coinTypeUnit).toLong()
    }
}