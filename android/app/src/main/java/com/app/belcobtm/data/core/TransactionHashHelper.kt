package com.app.belcobtm.data.core

import com.app.belcobtm.api.model.param.trx.Trx
import com.app.belcobtm.data.rest.wallet.WalletApiService
import com.app.belcobtm.data.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.db.DbCryptoCoin
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.*
import com.app.belcobtm.presentation.core.extensions.*
import com.google.gson.Gson
import com.google.protobuf.ByteString
import wallet.core.jni.*
import wallet.core.jni.proto.*
import java.math.BigDecimal
import java.util.*

class TransactionHashHelper(
    private val apiService: WalletApiService,
    private val prefsHelper: SharedPreferencesHelper
) {

    suspend fun createTransactionHash(
        fromCoin: CoinType,
        fromCoinAmount: Double,
        fromCoinDbModel: DbCryptoCoin?,
        toAddress: String
    ): Either<Failure, String> = when (fromCoin) {
        CoinType.BITCOIN,
        CoinType.BITCOINCASH,
        CoinType.LITECOIN -> createTransactionHashBTCorLTCorBTH(
            toAddress,
            fromCoin,
            fromCoinAmount,
            fromCoinDbModel
        )
        CoinType.ETHEREUM -> createTransactionHashETH(
            toAddress,
            fromCoinAmount,
            fromCoinDbModel
        )
        CoinType.XRP -> createTransactionHashXRP(
            toAddress,
            fromCoinAmount,
            fromCoinDbModel
        )
        CoinType.BINANCE -> createTransactionHashBNB(
            toAddress,
            fromCoinAmount,
            fromCoinDbModel
        )
        CoinType.TRON -> createTransactionHashTron(
            toAddress,
            fromCoinAmount,
            fromCoinDbModel
        )
        else -> Either.Left(Failure.MessageError("Wrong coin"))
    }

    private suspend fun createTransactionHashBTCorLTCorBTH(
        toAddress: String,
        fromCoin: CoinType,
        fromCoinAmount: Double,
        fromCoinDbModel: DbCryptoCoin?
    ): Either<Failure, String> {
        val hdWallet = HDWallet(prefsHelper.apiSeed, "")
        val publicKey = hdWallet.getExtendedPublicKey(
            fromCoin.customPurpose(),
            fromCoin,
            fromCoin.customXpubVersion()
        )
        val response = apiService.getUtxoList(fromCoin.code(), publicKey)

        return if (response.isRight) {
            val utxos = (response as Either.Right).b
            val publicKeyFrom = fromCoinDbModel?.publicKey
            val cryptoToSatoshi = fromCoinAmount * CoinType.BITCOIN.unit()
            val amount: Long = cryptoToSatoshi.toLong()
            val byteFee = getByteFee(fromCoinDbModel?.coinType)
            val sngHash = TWBitcoinSigHashType.getCryptoHash(fromCoin)
//        val cointypeValue = if (coinType.value() == 2) 0 else coinType.value()
            val cointypeValue = fromCoin.value()
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
                val redeemScript = BitcoinScript.buildForAddress(it.address, fromCoin)
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
                val redeemScript = BitcoinScript.buildForAddress(utxo.address, fromCoin)
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
            val hash = Numeric.toHexString(output.encoded.toByteArray())
            Either.Right(hash.substring(2))
        } else {
            response as Either.Left
        }
    }

    private fun getByteFee(coinName: String?): Long {
        val coinTypeUnit: Long = CoinTypeExtension.getTypeByCode(coinName ?: "")?.unit() ?: 0
        val byteFee = prefsHelper.coinsFee[coinName]?.byteFee ?: Double.MIN_VALUE
        return (byteFee * coinTypeUnit).toLong()
    }

    private suspend fun createTransactionHashETH(
        toAddress: String,
        fromCoinAmount: Double,
        fromCoinDbModel: DbCryptoCoin?
    ): Either<Failure, String> {
        val response = apiService.getEthereumNonce()
        return if (response.isRight) {
            val nonceResponse = (response as Either.Right).b
            val cryptoToSubcoin = BigDecimal(fromCoinAmount * CoinType.ETHEREUM.unit())
            val nonsStr: String = nonceResponse?.toString(16) ?: ""
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

            signingInput.also {
                it.privateKey = fromCoinDbModel?.privateKey?.toHexBytesInByteString()
                it.toAddress = toAddress
                it.chainId = ByteString.copyFrom("0x1".toHexByteArray())
                it.nonce = nonceHex
                it.gasPrice = gasPriceHex
                it.gasLimit = gasLimitHex
                it.amount = amountHex
            }

            val sign: Ethereum.SigningOutput = EthereumSigner.sign(signingInput.build())
            val resTransactionHashStr = Numeric.toHexString(sign.encoded.toByteArray())
            Either.Right(resTransactionHashStr)
        } else {
            response as Either.Left
        }
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

    private suspend fun createTransactionHashXRP(
        toAddress: String,
        fromCoinAmount: Double,
        fromCoinDbModel: DbCryptoCoin?
    ): Either<Failure, String> {
        val response = apiService.getRippleSequence()

        return if (response.isRight) {
            val privateKey = PrivateKey(fromCoinDbModel?.privateKey?.toHexByteArray())
            val signingInput = Ripple.SigningInput.newBuilder().also {
                it.sequence = (response as Either.Right).b.toInt()
                it.account = fromCoinDbModel?.publicKey ?: ""
                it.amount = (fromCoinAmount * CoinType.XRP.unit()).toLong()
                it.destination = toAddress
                it.fee = ((prefsHelper.coinsFee[CoinType.XRP.code()]?.txFee?.toBigDecimal()
                    ?: BigDecimal(0.000020)) * BigDecimal.valueOf(CoinType.XRP.unit())).toLong()
                it.privateKey = ByteString.copyFrom(privateKey.data())
            }.build()
            val signBytes = RippleSigner.sign(signingInput).encoded.toByteArray()
            val hash = Numeric.toHexString(signBytes)
            Either.Right(hash.substring(2))
        } else {
            response as Either.Left
        }
    }

    private suspend fun createTransactionHashBNB(
        toAddress: String,
        fromCoinAmount: Double,
        fromCoinDbModel: DbCryptoCoin?
    ): Either<Failure, String> {
        val response = apiService.getBinanceBlockHeader()

        return if (response.isRight) {
            val privateKey = PrivateKey(fromCoinDbModel?.privateKey?.toHexByteArray())
            val publicKey = privateKey.getPublicKeySecp256k1(true)

            val token = Binance.SendOrder.Token.newBuilder().also {
                it.denom = CoinType.BINANCE.code()
                it.amount = (fromCoinAmount * CoinType.BINANCE.unit()).toLong()
            }
            val input = Binance.SendOrder.Input.newBuilder().also {
                it.address = ByteString.copyFrom(CosmosAddress(HRP.BINANCE, publicKey).keyHash())
                it.addAllCoins(listOf(token.build()))
            }
            val output = Binance.SendOrder.Output.newBuilder().also {
                it.address = ByteString.copyFrom(CosmosAddress(toAddress).keyHash())
                it.addAllCoins(listOf(token.build()))
            }
            val sendOrder = Binance.SendOrder.newBuilder().also {
                it.addAllInputs(listOf(input.build()))
                it.addAllOutputs(listOf(output.build()))
            }
            val signingInput = Binance.SigningInput.newBuilder().also {
                it.chainId = "Binance-Chain-Tigris"
                it.accountNumber = (response as Either.Right).b.accountNumber ?: 0
                it.sequence = response.b.sequence ?: 0
                it.privateKey = ByteString.copyFrom(privateKey.data())
                it.sendOrder = sendOrder.build()
            }.build()
            val signBytes = BinanceSigner.sign(signingInput).encoded.toByteArray()
            val hash = Numeric.toHexString(signBytes)
            Either.Right(hash.substring(2))
        } else {
            response as Either.Left
        }
    }

    private suspend fun createTransactionHashTron(
        toAddress: String,
        fromCoinAmount: Double,
        fromCoinDbModel: DbCryptoCoin?
    ): Either<Failure, String> {
        val response = apiService.getTronBlockHeader(fromCoinDbModel?.coinType ?: "")

        return if (response.isRight) {
            val rawData = (response as Either.Right).b
            val cryptoToSubcoin = fromCoinAmount * CoinType.TRON.unit()
            val fromAddress = fromCoinDbModel?.publicKey
            val tronBlock = Tron.BlockHeader.newBuilder().also {
                it.number = rawData?.number ?: 0L
                it.timestamp = rawData?.timestamp ?: 0L
                it.version = rawData?.version ?: 0
                it.parentHash = rawData?.parentHash?.toHexBytesInByteString()
                it.witnessAddress = rawData?.witness_address?.toHexBytesInByteString()
                it.txTrieRoot = rawData?.txTrieRoot?.toHexBytesInByteString()
            }
            val transferBuilder = Tron.TransferContract.newBuilder().also {
                it.ownerAddress = fromAddress
                it.toAddress = toAddress
                it.amount = cryptoToSubcoin.toLong()
            }
            val transaction = Tron.Transaction.newBuilder().also {
                it.transfer = transferBuilder.build()
                it.timestamp = Date().time
                it.expiration = Calendar.getInstance().also { calendar ->
                    calendar.time = Date()
                    calendar.add(Calendar.HOUR, 10)
                }.timeInMillis
                it.feeLimit = ((prefsHelper.coinsFee[CoinType.TRON.code()]?.txFee?.toBigDecimal()
                    ?: BigDecimal(1)) * BigDecimal.valueOf(CoinType.TRON.unit())).toLong()
                it.blockHeader = tronBlock.build()
            }
            val signing = Tron.SigningInput.newBuilder().also {
                it.transaction = transaction.build()
                it.privateKey = fromCoinDbModel?.privateKey?.toHexBytesInByteString()
            }.build()
            val jsonHash = TronSigner.sign(signing).json
            val correctJson = Gson().toJson(Gson().fromJson<Trx>(jsonHash, Trx::class.java))
            Either.Right(correctJson)
        } else {
            response as Either.Left
        }
    }
}