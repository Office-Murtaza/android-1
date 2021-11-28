package com.belcobtm.data.core.factory

import com.belcobtm.R
import com.belcobtm.data.disk.database.account.AccountDao
import com.belcobtm.data.disk.database.wallet.WalletDao
import com.belcobtm.data.disk.database.wallet.toDataItem
import com.belcobtm.domain.Failure
import com.belcobtm.domain.transaction.item.TransactionPlanItem
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.presentation.core.Numeric
import com.belcobtm.presentation.core.extensions.USDC_UNIT
import com.belcobtm.presentation.core.extensions.toStringCoin
import com.belcobtm.presentation.core.extensions.unit
import com.belcobtm.presentation.core.provider.string.StringProvider
import com.belcobtm.presentation.core.toHexByteArray
import com.belcobtm.presentation.core.toHexBytesInByteString
import com.google.protobuf.ByteString
import wallet.core.java.AnySigner
import wallet.core.jni.CoinType
import wallet.core.jni.EthereumAbi
import wallet.core.jni.EthereumAbiFunction
import wallet.core.jni.proto.Ethereum
import java.math.BigDecimal

class EthTransactionInputBuilderFactory(
    private val walletDao: WalletDao,
    private val accountDao: AccountDao,
    private val stringProvider: StringProvider
) {

    suspend fun createForEth(
        toAddress: String,
        fromCoin: LocalCoinType,
        fromCoinAmount: Double,
        fromTransactionPlan: TransactionPlanItem
    ): String {
        val coinItem = walletDao.getCoinByCode(fromCoin.name).toDataItem()
        val amountMultipliedByDivider =
            BigDecimal(fromCoinAmount.toStringCoin().toDouble() * CoinType.ETHEREUM.unit())
        val transfer = Ethereum.Transaction.Transfer.newBuilder()
        transfer.amount = ByteString.copyFrom(
            "0x${amountMultipliedByDivider.toBigInteger().toString(16)}".toHexByteArray()
        )
        val transaction = Ethereum.Transaction.newBuilder()
        transaction.setTransfer(transfer)

        val input: Ethereum.SigningInput.Builder = createInput(
            coinItem.code, toAddress, fromTransactionPlan
        )
        input.setTransaction(transaction)
        val output =
            AnySigner.sign(input.build(), CoinType.ETHEREUM, Ethereum.SigningOutput.parser())
        return Numeric.toHexString(output.encoded.toByteArray())
    }

    suspend fun createForStaking(
        toAddress: String,
        fromCoin: LocalCoinType,
        fromCoinAmount: Double,
        fromTransactionPlan: TransactionPlanItem,
        ethereumAbiFunction: EthereumAbiFunction
    ): String {
        val coinItem = walletDao.getCoinByCode(fromCoin.name).toDataItem()
        val amountMultipliedByDivider =
            BigDecimal(fromCoinAmount.toStringCoin().toDouble() * CoinType.ETHEREUM.unit())
        val transfer = Ethereum.Transaction.Transfer.newBuilder()
        transfer.amount = ByteString.copyFrom(
            "0x${amountMultipliedByDivider.toBigInteger().toString(16)}".toHexByteArray()
        )
        transfer.data = ByteString.copyFrom(EthereumAbi.encode(ethereumAbiFunction))
        val transaction = Ethereum.Transaction.newBuilder()
        transaction.setTransfer(transfer)

        val input: Ethereum.SigningInput.Builder = createInput(
            coinItem.code, toAddress, fromTransactionPlan
        )
        input.setTransaction(transaction)
        val output =
            AnySigner.sign(input.build(), CoinType.ETHEREUM, Ethereum.SigningOutput.parser())
        return Numeric.toHexString(output.encoded.toByteArray())
    }

    suspend fun createForSubEth(
        toAddress: String,
        fromCoin: LocalCoinType,
        fromCoinAmount: Double,
        fromTransactionPlan: TransactionPlanItem
    ): String {
        val coinItem = walletDao.getCoinByCode(fromCoin.name).toDataItem()
        val erc20Transfer = Ethereum.Transaction.ERC20Transfer.newBuilder()
        erc20Transfer.to = toAddress
        val unit = when (fromCoin) {
            LocalCoinType.USDC -> USDC_UNIT
            else -> CoinType.ETHEREUM.unit()
        }
        val amountMultipliedByDivider = BigDecimal(fromCoinAmount.toStringCoin().toDouble() * unit)
        erc20Transfer.amount = ByteString.copyFrom(
            "0x${amountMultipliedByDivider.toBigInteger().toString(16)}".toHexByteArray()
        )
        val transaction = Ethereum.Transaction.newBuilder()
        transaction.setErc20Transfer(erc20Transfer)
        val input =
            createInput(coinItem.code, coinItem.details.contractAddress, fromTransactionPlan)
        input.setTransaction(transaction)

        val output =
            AnySigner.sign(input.build(), CoinType.ETHEREUM, Ethereum.SigningOutput.parser())

        return Numeric.toHexString(output.encoded.toByteArray())
    }

    private suspend fun createInput(
        coinCode: String,
        toAddress: String,
        transactionPlanItem: TransactionPlanItem
    ): Ethereum.SigningInput.Builder {
        val coinEntity = accountDao.getItem(coinCode)
        val privateKey = coinEntity.privateKey
        val input = Ethereum.SigningInput.newBuilder()
        val nonce = transactionPlanItem.nonce
        val gasPrice = transactionPlanItem.gasPrice
        val gasLimit = transactionPlanItem.gasLimit
        val hexNonce = "0x${nonce.toString(16)}".toHexByteArray()
        val hexGasLimit = "0x${gasLimit.toString(16)}".toHexByteArray()
        val hexGasPrice = "0x${gasPrice.toString(16)}".toHexByteArray()

        if (coinEntity.publicKey == toAddress) {
            throw Failure.MessageError(stringProvider.getString(R.string.addresses_match_singing_error))
        }

        input.privateKey = privateKey.toHexBytesInByteString()
        input.toAddress = toAddress
        input.chainId = ByteString.copyFrom("0x${transactionPlanItem.chainId}".toHexByteArray())
        input.nonce = ByteString.copyFrom(hexNonce)
        input.gasPrice = ByteString.copyFrom(hexGasPrice)
        input.gasLimit = ByteString.copyFrom(hexGasLimit)

        return input
    }
}