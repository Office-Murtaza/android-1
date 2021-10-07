package com.belcobtm.data.core.factory

import com.belcobtm.data.disk.database.account.AccountDao
import com.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.belcobtm.data.rest.transaction.response.hash.UtxoItemResponse
import com.belcobtm.domain.transaction.item.TransactionPlanItem
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.presentation.core.Numeric
import com.belcobtm.presentation.core.extensions.unit
import com.belcobtm.presentation.core.toHexBytes
import com.google.protobuf.ByteString
import wallet.core.jni.BitcoinScript
import wallet.core.jni.CoinType
import wallet.core.jni.HDWallet
import wallet.core.jni.proto.Bitcoin

class BlockTransactionInputBuilderFactory(
    private val prefsHelper: SharedPreferencesHelper,
    private val daoAccount: AccountDao
) {

    suspend fun createInput(
        utxos: List<UtxoItemResponse>,
        toAddress: String,
        fromCoin: LocalCoinType,
        fromCoinAmount: Double,
        fromTransactionPlan: TransactionPlanItem
    ): Bitcoin.SigningInput.Builder {
        val trustWalletCoin = fromCoin.trustWalletType
        val hdWallet = HDWallet(prefsHelper.apiSeed, "")
        val fromAddress = daoAccount.getItem(fromCoin.name).publicKey
        val cryptoToSatoshi = fromCoinAmount * CoinType.BITCOIN.unit()
        val amount: Long = cryptoToSatoshi.toLong()
        val byteFee = fromTransactionPlan.byteFee
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

        return input
    }
}