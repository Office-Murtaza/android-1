package com.app.belcobtm.data.core

import com.app.belcobtm.api.model.param.trx.Trx
import com.app.belcobtm.data.disk.database.CoinDao
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.rest.transaction.TransactionApiService
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.presentation.core.*
import com.app.belcobtm.presentation.core.extensions.*
import com.google.gson.Gson
import com.google.protobuf.ByteString
import wallet.core.java.AnySigner
import wallet.core.jni.*
import wallet.core.jni.proto.*
import java.math.BigDecimal
import java.util.*


class TransactionHashHelper(
    private val apiService: TransactionApiService,
    private val prefsHelper: SharedPreferencesHelper,
    private val daoCoin: CoinDao
) {

    suspend fun createTransactionHash(
        fromCoin: LocalCoinType,
        fromCoinAmount: Double,
        toAddress: String
    ): Either<Failure, String> = when (fromCoin) {
        LocalCoinType.BTC,
        LocalCoinType.BCH,
        LocalCoinType.LTC -> createTransactionHashBTCorLTCorBTH(
            toAddress,
            fromCoin,
            fromCoinAmount
        )
        LocalCoinType.ETH,
        LocalCoinType.CATM -> createTransactionHashETH(
            toAddress,
            fromCoin,
            fromCoinAmount
        )
        LocalCoinType.XRP -> createTransactionHashXRP(
            toAddress,
            fromCoin,
            fromCoinAmount
        )
        LocalCoinType.BNB -> createTransactionHashBNB(
            toAddress,
            fromCoin,
            fromCoinAmount
        )
        LocalCoinType.TRX -> createTransactionHashTron(
            toAddress,
            fromCoin,
            fromCoinAmount
        )
    }

    suspend fun createTransactionStakeHash(
        fromCoinAmount: Double,
        toAddress: String
    ) = createTransactionHashETH(
        toAddress,
        LocalCoinType.CATM,
        fromCoinAmount,
        ETH_CATM_FUNCTION_NAME_CREATE_STAKE
    )

    suspend fun createTransactionUnStakeHash(
        fromCoinAmount: Double,
        toAddress: String
    ) = createTransactionHashETH(
        toAddress,
        LocalCoinType.CATM,
        fromCoinAmount,
        ETH_CATM_FUNCTION_NAME_WITHDRAW_STAKE
    )

    private suspend fun createTransactionHashBTCorLTCorBTH(
        toAddress: String,
        fromCoin: LocalCoinType,
        fromCoinAmount: Double
    ): Either<Failure, String> {
        val trustWalletCoin = fromCoin.trustWalletType
        val hdWallet = HDWallet(prefsHelper.apiSeed, "")
        val publicKey = hdWallet.getExtendedPublicKey(
            trustWalletCoin.customPurpose(),
            trustWalletCoin,
            trustWalletCoin.customXpubVersion()
        )
        val response = apiService.getUtxoList(fromCoin.name, publicKey)

        return if (response.isRight) {
            val utxos = (response as Either.Right).b
            val publicKeyFrom = daoCoin.getItem(fromCoin.name).publicKey
            val cryptoToSatoshi = fromCoinAmount * CoinType.BITCOIN.unit()
            val amount: Long = cryptoToSatoshi.toLong()
            val byteFee = getByteFee(fromCoin.name)
            val sngHash = TWBitcoinSigHashType.getCryptoHash(fromCoin.trustWalletType)
            val cointypeValue = fromCoin.trustWalletType.value()
            val input = Bitcoin.SigningInput.newBuilder()
                .setAmount(amount)
                .setHashType(sngHash)
                .setToAddress(toAddress)
                .setChangeAddress(publicKeyFrom)
                .setByteFee(byteFee)
                .setCoinType(cointypeValue)

            utxos.forEach {
                val privateKey = hdWallet.getKey(it.path)
                input.addPrivateKey(ByteString.copyFrom(privateKey.data()))
            }

            utxos.forEach {
                val redeemScript = BitcoinScript.buildForAddress(it.address, trustWalletCoin)
                val keyHash = if (redeemScript.isPayToWitnessScriptHash) {
                    redeemScript.matchPayToWitnessPublicKeyHash()
                } else {
                    redeemScript.matchPayToPubkeyHash()
                }

                if (keyHash != null && keyHash.isNotEmpty()) {
                    val key = Numeric.toHexString(keyHash)
                    val scriptByteString = ByteString.copyFrom(redeemScript.data())
                    input.putScripts(key, scriptByteString)
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
                val redeemScript = BitcoinScript.buildForAddress(utxo.address, trustWalletCoin)
                val scriptByteString = ByteString.copyFrom(redeemScript.data())
                val utxo0 = Bitcoin.UnspentTransaction.newBuilder()
                    .setScript(scriptByteString)
                    .setAmount(utxoAmount)
                    .setOutPoint(outpoint)
                    .build()

                input.addUtxo(utxo0)
            }

            val signBytes = AnySigner.sign(
                input.build(),
                CoinType.BITCOIN,
                Bitcoin.SigningOutput.parser()
            ).encoded.toByteArray()
            val hash = Numeric.toHexString(signBytes)
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
        fromCoin: LocalCoinType,
        fromCoinAmount: Double,
        customFunctionName: String? = null
    ): Either<Failure, String> {
        val nonceAddress = daoCoin.getItem(fromCoin.name).publicKey
        val response = apiService.getEthereumNonce(fromCoin.name, nonceAddress)

        return if (response.isRight) {
            val nonceResponse = (response as Either.Right).b
            val coinFee = prefsHelper.coinsFee[fromCoin.name]
            val amountMultipliedByDivider = BigDecimal(fromCoinAmount * CoinType.ETHEREUM.unit())
            val hexAmount = addLeadingZeroes(amountMultipliedByDivider.toLong().toString(16))?.toHexByteArray()
            val hexNonce = addLeadingZeroes(nonceResponse?.toString(16) ?: "")?.toHexByteArray()
            val hexGasLimit = addLeadingZeroes((coinFee?.gasLimit?.toLong() ?: 0).toString(16))?.toHexByteArray()
            val hexGasPrice = addLeadingZeroes((coinFee?.gasPrice?.toLong() ?: 0).toString(16))?.toHexByteArray()
            val input = Ethereum.SigningInput.newBuilder().also {
                it.chainId = ByteString.copyFrom("0x1".toHexByteArray())
                it.nonce = ByteString.copyFrom(hexNonce)
                it.gasLimit = ByteString.copyFrom(hexGasLimit)
                it.gasPrice = ByteString.copyFrom(hexGasPrice)
                it.privateKey = daoCoin.getItem(fromCoin.name).privateKey.toHexBytesInByteString()
            }

            if (fromCoin == LocalCoinType.CATM) {
                val function = when (customFunctionName) {
                    ETH_CATM_FUNCTION_NAME_WITHDRAW_STAKE ->
                        EthereumAbiEncoder.buildFunction(ETH_CATM_FUNCTION_NAME_WITHDRAW_STAKE)
                    ETH_CATM_FUNCTION_NAME_CREATE_STAKE -> {
                        val function = EthereumAbiEncoder.buildFunction(ETH_CATM_FUNCTION_NAME_CREATE_STAKE)
                        function.addParamAddress(toAddress.toHexByteArray(), false)
                        function.addParamUInt256(amountMultipliedByDivider.toBigInteger().toByteArray(), false)
                        function
                    }
                    else -> {
                        val function = EthereumAbiEncoder.buildFunction(ETH_CATM_FUNCTION_NAME_TRANSFER)
                        function.addParamAddress(toAddress.toHexByteArray(), false)
                        function.addParamUInt256(amountMultipliedByDivider.toBigInteger().toByteArray(), false)
                        function
                    }
                }

                input.payload = ByteString.copyFrom(EthereumAbiEncoder.encode(function))
                input.toAddress = coinFee?.contractAddress
            } else {
                input.amount = ByteString.copyFrom(hexAmount)
                input.toAddress = toAddress
            }

            val output = AnySigner.sign(input.build(), CoinType.ETHEREUM, Ethereum.SigningOutput.parser())
            val transactionHash = Numeric.toHexString(output.encoded.toByteArray())
            Either.Right(transactionHash)
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
        fromCoin: LocalCoinType,
        fromCoinAmount: Double
    ): Either<Failure, String> {
        val response = apiService.getRippleSequence(toAddress)
        val coinEntity = daoCoin.getItem(fromCoin.name)

        return if (response.isRight) {
            val privateKey = PrivateKey(coinEntity.privateKey.toHexByteArray())
            val signingInput = Ripple.SigningInput.newBuilder().also {
                it.sequence = (response as Either.Right).b.toInt()
                it.account = coinEntity.publicKey
                it.amount = (fromCoinAmount * CoinType.XRP.unit()).toLong()
                it.destination = toAddress
                it.fee = ((prefsHelper.coinsFee[CoinType.XRP.code()]?.txFee?.toBigDecimal()
                    ?: BigDecimal(0.000020)) * BigDecimal.valueOf(CoinType.XRP.unit())).toLong()
                it.privateKey = ByteString.copyFrom(privateKey.data())
            }.build()
            val signBytes = AnySigner.sign(
                signingInput,
                CoinType.XRP,
                Ripple.SigningOutput.parser()
            ).encoded.toByteArray()
            val hash = Numeric.toHexString(signBytes)
            Either.Right(hash.substring(2))
        } else {
            response as Either.Left
        }
    }

    private suspend fun createTransactionHashBNB(
        toAddress: String,
        fromCoin: LocalCoinType,
        fromCoinAmount: Double
    ): Either<Failure, String> {
        val response = apiService.getBinanceBlockHeader(toAddress)
        val coinEntity = daoCoin.getItem(fromCoin.name)

        return if (response.isRight) {
            val privateKey = PrivateKey(coinEntity.privateKey.toHexByteArray())
            val publicKey = privateKey.getPublicKeySecp256k1(true)

            val token = Binance.SendOrder.Token.newBuilder().also {
                it.denom = CoinType.BINANCE.code()
                it.amount = (fromCoinAmount * CoinType.BINANCE.unit()).toLong()
            }
            val input = Binance.SendOrder.Input.newBuilder().also {
                it.address = ByteString.copyFrom(AnyAddress(publicKey, CoinType.BINANCE).data())
                it.addAllCoins(listOf(token.build()))
            }
            val output = Binance.SendOrder.Output.newBuilder().also {
                it.address = ByteString.copyFrom(AnyAddress(toAddress, CoinType.BINANCE).data())
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

            val signBytes = AnySigner.sign(
                signingInput,
                CoinType.BINANCE,
                Binance.SigningOutput.parser()
            ).encoded.toByteArray()
            val hash = Numeric.toHexString(signBytes)
            Either.Right(hash.substring(2))
        } else {
            response as Either.Left
        }
    }

    private suspend fun createTransactionHashTron(
        toAddress: String,
        fromCoin: LocalCoinType,
        fromCoinAmount: Double
    ): Either<Failure, String> {
        val response = apiService.getTronBlockHeader(fromCoin.name)
        val coinEntity = daoCoin.getItem(fromCoin.name)

        return if (response.isRight) {
            val rawData = (response as Either.Right).b
            val cryptoToSubcoin = fromCoinAmount * CoinType.TRON.unit()
            val fromAddress = coinEntity.publicKey
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
            val signingInput = Tron.SigningInput.newBuilder().also {
                it.transaction = transaction.build()
                it.privateKey = coinEntity.privateKey.toHexBytesInByteString()
            }.build()

            val signJson = AnySigner.sign(signingInput, CoinType.TRON, Tron.SigningOutput.parser()).json
            val correctJson = Gson().toJson(Gson().fromJson(signJson, Trx::class.java))
            Either.Right(correctJson)
        } else {
            response as Either.Left
        }
    }

    private companion object {
        private const val ETH_CATM_FUNCTION_NAME_TRANSFER: String = "transfer"
        private const val ETH_CATM_FUNCTION_NAME_CREATE_STAKE: String = "createStake"
        private const val ETH_CATM_FUNCTION_NAME_WITHDRAW_STAKE: String = "withdrawStake"
    }
}