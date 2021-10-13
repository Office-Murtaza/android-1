package com.belcobtm.data.core.factory

import com.belcobtm.data.disk.database.account.AccountDao
import com.belcobtm.data.disk.database.wallet.WalletDao
import com.belcobtm.data.disk.database.wallet.toDataItem
import com.belcobtm.domain.transaction.item.TransactionPlanItem
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.presentation.core.Numeric
import com.belcobtm.presentation.core.extensions.USDC_UNIT
import com.belcobtm.presentation.core.extensions.unit
import com.belcobtm.presentation.core.toHexByteArray
import com.google.protobuf.ByteString
import wallet.core.java.AnySigner
import wallet.core.jni.CoinType
import wallet.core.jni.PrivateKey
import wallet.core.jni.proto.Ethereum
import java.math.BigDecimal

class EthTransactionInputBuilderFactory(
    private val walletDao: WalletDao,
    private val accountDao: AccountDao,
) {

    suspend fun createForEth(
        toAddress: String,
        fromCoin: LocalCoinType,
        fromCoinAmount: Double,
        fromTransactionPlan: TransactionPlanItem
    ): String {
        val coinItem = walletDao.getCoinByCode(fromCoin.name).toDataItem()
        val amountMultipliedByDivider = BigDecimal(fromCoinAmount * CoinType.ETHEREUM.unit())
            .toLong().toString(16)
        val transfer = Ethereum.Transaction.Transfer.newBuilder()
        transfer.amount = ByteString.copyFrom(
            Numeric.hexStringToByteArray("0x${amountMultipliedByDivider}")
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
        val amountMultipliedByDivider = BigDecimal(fromCoinAmount * unit)
            .toLong().toString(16)
        erc20Transfer.amount = ByteString.copyFrom(
            Numeric.hexStringToByteArray("0x${amountMultipliedByDivider}")
        )

        val transaction = Ethereum.Transaction.newBuilder()
        transaction.setErc20Transfer(erc20Transfer)

        val input = createInput(coinItem.code, toAddress, fromTransactionPlan)
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
        val privateKey = PrivateKey(coinEntity.privateKey.toHexByteArray())
        val input = Ethereum.SigningInput.newBuilder()
        val nonce = transactionPlanItem.nonce
        val gasPrice = transactionPlanItem.gasPrice
        val gasLimit = transactionPlanItem.gasLimit
        val hexNonce = addLeadingZeroes(nonce.toString(16)).toHexByteArray()
        val hexGasLimit = addLeadingZeroes(gasLimit.toString(16))
            .toHexByteArray()
        val hexGasPrice = addLeadingZeroes(gasPrice.toString(16))
            .toHexByteArray()

        input.privateKey = ByteString.copyFrom(privateKey.data())
        input.toAddress = toAddress
        input.chainId = ByteString.copyFrom("0x1".toHexByteArray())
        input.nonce = ByteString.copyFrom(hexNonce)
        input.gasPrice = ByteString.copyFrom(hexGasPrice)
        input.gasLimit = ByteString.copyFrom(hexGasLimit)

        return input
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
}