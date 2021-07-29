package com.belcobtm.data.core

import com.belcobtm.data.core.trx.Trx
import com.belcobtm.data.disk.database.account.AccountDao
import com.belcobtm.data.disk.database.wallet.WalletDao
import com.belcobtm.data.disk.database.wallet.toDataItem
import com.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.belcobtm.data.rest.transaction.TransactionApiService
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.domain.wallet.item.CoinDataItem
import com.belcobtm.domain.wallet.item.isEthRelatedCoinCode
import com.belcobtm.presentation.core.Numeric
import com.belcobtm.presentation.core.extensions.*
import com.belcobtm.presentation.core.toHexByteArray
import com.belcobtm.presentation.core.toHexBytes
import com.belcobtm.presentation.core.toHexBytesInByteString
import com.google.protobuf.ByteString
import com.squareup.moshi.Moshi
import wallet.core.java.AnySigner
import wallet.core.jni.*
import wallet.core.jni.proto.*
import java.math.BigDecimal
import java.util.*


class TransactionHashHelper(
    private val moshi: Moshi,
    private val walletDao: WalletDao,
    private val apiService: TransactionApiService,
    private val prefsHelper: SharedPreferencesHelper,
    private val daoAccount: AccountDao
) {

    suspend fun createTransactionHash(
        fromCoin: LocalCoinType,
        fromCoinAmount: Double,
        toAddress: String
    ): Either<Failure, String> = when (fromCoin) {
        LocalCoinType.BTC,
        LocalCoinType.BCH,
        LocalCoinType.DOGE,
        LocalCoinType.DASH,
        LocalCoinType.LTC -> createTransactionHashBTCorLTCorBTH(
            toAddress,
            fromCoin,
            fromCoinAmount
        )
        LocalCoinType.ETH,
        LocalCoinType.USDC,
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

    suspend fun createTransactionStakeCancelHash(
        fromCoinAmount: Double,
        toAddress: String
    ) = createTransactionHashETH(
        toAddress,
        LocalCoinType.CATM,
        fromCoinAmount,
        ETH_CATM_FUNCTION_NAME_CANCEL_STAKE
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
            val fromAddress = daoAccount.getItem(fromCoin.name).publicKey
            val cryptoToSatoshi = fromCoinAmount * CoinType.BITCOIN.unit()
            val amount: Long = cryptoToSatoshi.toLong()
            val byteFee = getByteFee(fromCoin.name)
            val sngHash = BitcoinScript.hashTypeForCoin(trustWalletCoin)
            val coinTypeValue = trustWalletCoin.value()
            val input = Bitcoin.SigningInput.newBuilder()
                .setCoinType(coinTypeValue)
                .setAmount(amount)
                .setByteFee(byteFee)
                .setHashType(sngHash)
                .setChangeAddress(fromAddress)
                .setToAddress(toAddress)
                .setUseMaxAmount(false)

            utxos.forEach {
                val privateKey = hdWallet.getKey(trustWalletCoin, it.path)
                input.addPrivateKey(ByteString.copyFrom(privateKey.data()))
            }

            utxos.forEach {
                val redeemScript = BitcoinScript.lockScriptForAddress(it.address, trustWalletCoin)
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
                val redeemScript = BitcoinScript.lockScriptForAddress(utxo.address, trustWalletCoin)
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
                trustWalletCoin,
                Bitcoin.SigningOutput.parser()
            ).encoded.toByteArray()
            val hash = Numeric.toHexString(signBytes)
            Either.Right(hash.substring(2))
        } else {
            response as Either.Left
        }
    }

    private suspend fun getByteFee(coinName: String?): Long {
        if (coinName == null) {
            return Long.MIN_VALUE
        }
        return getCoinByCode(coinName)?.details?.byteFee ?: Long.MIN_VALUE
    }

    private suspend fun createTransactionHashETH(
        toAddress: String,
        fromCoin: LocalCoinType,
        fromCoinAmount: Double,
        customFunctionName: String? = null
    ): Either<Failure, String> {
        val nonceAddress = daoAccount.getItem(fromCoin.name).publicKey
        val response = apiService.getEthereumNonce(nonceAddress)

        return if (response.isRight) {
            val nonceResponse = (response as Either.Right).b
            val coinItem = getCoinByCode(fromCoin.name)
            val amountMultipliedByDivider = BigDecimal(
                fromCoinAmount * when (fromCoin) {
                    LocalCoinType.USDC -> USDC_UNIT
                    else -> CoinType.ETHEREUM.unit()
                }
            )
            // todo find out what should be used for gasLimit / gasPrice
            val hexAmount =
                addLeadingZeroes(amountMultipliedByDivider.toLong().toString(16)).toHexByteArray()
            val hexNonce = addLeadingZeroes(nonceResponse?.toString(16) ?: "").toHexByteArray()
            val hexGasLimit = addLeadingZeroes((coinItem.details.gasLimit ?: 0).toString(16))
                .toHexByteArray()
            val hexGasPrice = addLeadingZeroes((coinItem.details.gasPrice ?: 0).toString(16))
                .toHexByteArray()
            val input = Ethereum.SigningInput.newBuilder().also {
                it.chainId = ByteString.copyFrom("0x1".toHexByteArray())
                it.nonce = ByteString.copyFrom(hexNonce)
                it.gasLimit = ByteString.copyFrom(hexGasLimit)
                it.gasPrice = ByteString.copyFrom(hexGasPrice)
                it.privateKey =
                    daoAccount.getItem(fromCoin.name).privateKey.toHexBytesInByteString()
            }

            if (fromCoin.name.isEthRelatedCoinCode()) {
                val function = when (customFunctionName) {
                    ETH_CATM_FUNCTION_NAME_CREATE_STAKE -> {
                        val function = EthereumAbiFunction(ETH_CATM_FUNCTION_NAME_CREATE_STAKE)
                        function.addParamUInt256(
                            amountMultipliedByDivider.toBigInteger().toByteArray(), false
                        )
                        function
                    }
                    ETH_CATM_FUNCTION_NAME_CANCEL_STAKE -> EthereumAbiFunction(
                        ETH_CATM_FUNCTION_NAME_CANCEL_STAKE
                    )
                    ETH_CATM_FUNCTION_NAME_WITHDRAW_STAKE -> EthereumAbiFunction(
                        ETH_CATM_FUNCTION_NAME_WITHDRAW_STAKE
                    )
                    else -> {
                        val function = EthereumAbiFunction(ETH_CATM_FUNCTION_NAME_TRANSFER)
                        function.addParamAddress(toAddress.toHexByteArray(), false)
                        function.addParamUInt256(
                            amountMultipliedByDivider.toBigInteger().toByteArray(), false
                        )
                        function
                    }
                }
                input.payload = ByteString.copyFrom(EthereumAbi.encode(function))
                input.toAddress = coinItem?.publicKey
            } else {
                input.amount = ByteString.copyFrom(hexAmount)
                input.toAddress = toAddress
            }

            val output =
                AnySigner.sign(input.build(), CoinType.ETHEREUM, Ethereum.SigningOutput.parser())
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
    private fun addLeadingZeroes(str: String): String {
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
        val coinEntity = daoAccount.getItem(fromCoin.name)
        val checkActivationResponse = apiService.checkRippleAccountActivation(toAddress)

        return if (checkActivationResponse.isRight) {
            if ((checkActivationResponse as Either.Right).b) {
                println("MAMAMA = " + checkActivationResponse.b)

                val response = apiService.getRippleSequence(coinEntity.publicKey)

                return if (response.isRight) {
                    val privateKey = PrivateKey(coinEntity.privateKey.toHexByteArray())
                    val signingInput = Ripple.SigningInput.newBuilder().also {
                        it.sequence = (response as Either.Right).b.toInt()
                        it.account = coinEntity.publicKey
                        it.amount = (fromCoinAmount * CoinType.XRP.unit()).toLong()
                        it.destination = toAddress
                        it.fee =
                            ((getCoinByCode(CoinType.XRP.code())?.details?.txFee?.toBigDecimal()
                                ?: BigDecimal(0.000020)) * BigDecimal.valueOf(CoinType.XRP.unit())).toLong()
                        it.privateKey = ByteString.copyFrom(privateKey.data())
                    }
                    val signBytes = AnySigner.sign(
                        signingInput.build(),
                        CoinType.XRP,
                        Ripple.SigningOutput.parser()
                    ).encoded.toByteArray()
                    val hash = Numeric.toHexString(signBytes).substring(2)
                    Either.Right(hash)
                } else {
                    response as Either.Left
                }
            } else {
                Either.Left(Failure.XRPLowAmountToSend)
            }
        } else {
            checkActivationResponse as Either.Left
        }
    }

    private suspend fun createTransactionHashBNB(
        toAddress: String,
        fromCoin: LocalCoinType,
        fromCoinAmount: Double
    ): Either<Failure, String> {
        val coinEntity = daoAccount.getItem(fromCoin.name)
        val response = apiService.getBinanceBlockHeader(coinEntity.publicKey)

        return if (response.isRight) {
            val privateKey = PrivateKey(coinEntity.privateKey.toHexByteArray())
            val fromAddress = privateKey.getPublicKeySecp256k1(true)

            val token = Binance.SendOrder.Token.newBuilder().also {
                it.denom = CoinType.BINANCE.code()
                it.amount = (fromCoinAmount * CoinType.BINANCE.unit()).toLong()
            }
            val input = Binance.SendOrder.Input.newBuilder().also {
                it.address = ByteString.copyFrom(AnyAddress(fromAddress, CoinType.BINANCE).data())
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
            }

            val signBytes = AnySigner.sign(
                signingInput.build(),
                CoinType.BINANCE,
                Binance.SigningOutput.parser()
            ).encoded.toByteArray()
            val hash = Numeric.toHexString(signBytes).substring(2)
            Either.Right(hash)
        } else {
            response as Either.Left
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun createTransactionHashTron(
        toAddress: String,
        fromCoin: LocalCoinType,
        fromCoinAmount: Double
    ): Either<Failure, String> {
        val response = apiService.getTronBlockHeader()
        val coinEntity = daoAccount.getItem(fromCoin.name)

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
                it.blockHeader = tronBlock.build()
            }
            val signingInput = Tron.SigningInput.newBuilder().also {
                it.transaction = transaction.build()
                it.privateKey = coinEntity.privateKey.toHexBytesInByteString()
            }.build()

            val signJson =
                AnySigner.sign(signingInput, CoinType.TRON, Tron.SigningOutput.parser()).json
            val adapter = moshi.adapter(Trx::class.java)
            val jsonContent = adapter.fromJson(signJson)
            val correctJson = adapter.toJson(jsonContent)
            Either.Right(correctJson)
        } else {
            response as Either.Left
        }
    }

    private suspend fun getCoinByCode(coinCode: String): CoinDataItem =
        walletDao.getCoinByCode(coinCode).toDataItem()

    private companion object {
        private const val ETH_CATM_FUNCTION_NAME_TRANSFER: String = "transfer"
        private const val ETH_CATM_FUNCTION_NAME_CREATE_STAKE: String = "createStake"
        private const val ETH_CATM_FUNCTION_NAME_CANCEL_STAKE: String = "cancelStake"
        private const val ETH_CATM_FUNCTION_NAME_WITHDRAW_STAKE: String = "withdrawStake"
    }
}