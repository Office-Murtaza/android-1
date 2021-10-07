package com.belcobtm.data.core.factory

import com.belcobtm.data.disk.database.account.AccountDao
import com.belcobtm.data.disk.database.wallet.WalletDao
import com.belcobtm.data.disk.database.wallet.toDataItem
import com.belcobtm.domain.transaction.item.TransactionPlanItem
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.domain.wallet.item.isEthRelatedCoinCode
import com.belcobtm.presentation.core.extensions.USDC_UNIT
import com.belcobtm.presentation.core.extensions.unit
import com.belcobtm.presentation.core.toHexByteArray
import com.belcobtm.presentation.core.toHexBytesInByteString
import com.google.protobuf.ByteString
import wallet.core.jni.CoinType
import wallet.core.jni.EthereumAbi
import wallet.core.jni.EthereumAbiFunction
import wallet.core.jni.proto.Ethereum
import java.math.BigDecimal

class EthTransactionInputBuilderFactory(
    private val walletDao: WalletDao,
    private val accountDao: AccountDao,
) {

    companion object {
        const val ETH_CATM_FUNCTION_NAME_TRANSFER: String = "transfer"
        const val ETH_CATM_FUNCTION_NAME_CREATE_STAKE: String = "createStake"
        const val ETH_CATM_FUNCTION_NAME_CANCEL_STAKE: String = "cancelStake"
        const val ETH_CATM_FUNCTION_NAME_WITHDRAW_STAKE: String = "withdrawStake"
    }

    suspend fun createInput(
        toAddress: String,
        fromCoin: LocalCoinType,
        fromCoinAmount: Double,
        fromTransactionPlan: TransactionPlanItem,
        customFunctionName: String? = null
    ): Ethereum.SigningInput.Builder {
        val nonce = fromTransactionPlan.nonce
        val coinItem = walletDao.getCoinByCode(fromCoin.name).toDataItem()
        val amountMultipliedByDivider = BigDecimal(
            fromCoinAmount * when (fromCoin) {
                LocalCoinType.USDC -> USDC_UNIT
                else -> CoinType.ETHEREUM.unit()
            }
        )
        // todo find out what should be used for gasLimit / gasPrice
        val hexAmount =
            addLeadingZeroes(amountMultipliedByDivider.toLong().toString(16)).toHexByteArray()
        val hexNonce = addLeadingZeroes(nonce.toString(16)).toHexByteArray()
        val hexGasLimit = addLeadingZeroes((0).toString(16))
            .toHexByteArray()
        val hexGasPrice = addLeadingZeroes((0).toString(16))
            .toHexByteArray()
        val input = Ethereum.SigningInput.newBuilder().also {
            it.chainId = ByteString.copyFrom("0x1".toHexByteArray())
            it.nonce = ByteString.copyFrom(hexNonce)
            it.gasLimit = ByteString.copyFrom(hexGasLimit)
            it.gasPrice = ByteString.copyFrom(hexGasPrice)
            it.privateKey =
                accountDao.getItem(fromCoin.name).privateKey.toHexBytesInByteString()
        }

        val transfer = Ethereum.Transaction.Transfer.newBuilder()
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
            transfer.data = ByteString.copyFrom(EthereumAbi.encode(function))
            input.toAddress = coinItem.publicKey
        } else {
            transfer.amount = ByteString.copyFrom(hexAmount)
            input.toAddress = toAddress
        }
        input.transaction = Ethereum.Transaction.newBuilder()
            .setTransfer(transfer.build())
            .build()
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