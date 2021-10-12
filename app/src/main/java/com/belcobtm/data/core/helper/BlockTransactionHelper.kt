package com.belcobtm.data.core.helper

import com.belcobtm.data.core.factory.BlockTransactionInputBuilderFactory
import com.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.belcobtm.data.rest.transaction.TransactionApiService
import com.belcobtm.data.rest.transaction.response.hash.UtxoItemResponse
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.mapSuspend
import com.belcobtm.domain.transaction.item.SignedTransactionPlanItem
import com.belcobtm.domain.transaction.item.TransactionPlanItem
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.presentation.core.Numeric
import com.belcobtm.presentation.core.extensions.customPurpose
import com.belcobtm.presentation.core.extensions.customXpubVersion
import com.belcobtm.presentation.core.extensions.unit
import wallet.core.java.AnySigner
import wallet.core.jni.HDWallet
import wallet.core.jni.proto.Bitcoin


class BlockTransactionHelper(
    private val blockFactory: BlockTransactionInputBuilderFactory,
    private val preferencesHelper: SharedPreferencesHelper,
    private val transactionApiService: TransactionApiService
) {

    suspend fun getSignedTransactionPlan(
        useMaxAmountFlag: Boolean,
        toAddress: String,
        fromCoin: LocalCoinType,
        fromCoinAmount: Double,
        fromTransactionPlan: TransactionPlanItem
    ): Either<Failure, SignedTransactionPlanItem> = fetchUtxoAndPerform(fromCoin) { utxos ->
        val input = blockFactory.createInput(
            utxos, toAddress, fromCoin,
            fromCoinAmount, fromTransactionPlan
        )
        input.useMaxAmount = useMaxAmountFlag
        input.amount = (fromCoinAmount * fromCoin.trustWalletType.unit()).toLong()
        val plan = AnySigner.plan(
            input.build(),
            fromCoin.trustWalletType,
            Bitcoin.TransactionPlan.parser()
        )
        val availableAmount = plan.availableAmount.toDouble() / fromCoin.trustWalletType.unit()
        val fee = plan.fee / fromCoin.trustWalletType.unit().toDouble()
        SignedTransactionPlanItem(fee, availableAmount)
    }

    suspend fun getHash(
        toAddress: String,
        fromCoin: LocalCoinType,
        fromCoinAmount: Double,
        fromTransactionPlan: TransactionPlanItem
    ): Either<Failure, String> = fetchUtxoAndPerform(fromCoin) { utxos ->
        val input = blockFactory.createInput(
            utxos, toAddress, fromCoin,
            fromCoinAmount, fromTransactionPlan
        )
        val signBytes = AnySigner.sign(
            input.build(),
            fromCoin.trustWalletType,
            Bitcoin.SigningOutput.parser()
        ).encoded.toByteArray()
        Numeric.toHexString(signBytes).substring(2)
    }

    private suspend fun <T> fetchUtxoAndPerform(
        localCoin: LocalCoinType,
        onUtxoFetched: suspend (List<UtxoItemResponse>) -> T
    ): Either<Failure, T> {
        val coin = localCoin.trustWalletType
        val hdWallet = HDWallet(preferencesHelper.apiSeed, "")
        val publicKey = hdWallet.getExtendedPublicKey(
            coin.customPurpose(), coin, coin.customXpubVersion()
        )
        return transactionApiService.getUtxoList(localCoin.name, publicKey).mapSuspend {
            onUtxoFetched(it)
        }
    }
}