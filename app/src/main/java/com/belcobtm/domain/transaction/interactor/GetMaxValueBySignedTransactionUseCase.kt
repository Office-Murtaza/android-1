package com.belcobtm.domain.transaction.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.map
import com.belcobtm.domain.transaction.TransactionRepository
import com.belcobtm.domain.transaction.item.AmountWithFeeItem
import com.belcobtm.domain.transaction.item.TransactionPlanItem
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.domain.wallet.item.CoinDataItem

class GetMaxValueBySignedTransactionUseCase(
    private val transactionRepository: TransactionRepository
) : UseCase<AmountWithFeeItem, GetMaxValueBySignedTransactionUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, AmountWithFeeItem> =
        transactionRepository.getSignedPlan(
            fromCoin = params.coinDataItem.code,
            fromCoinAmount = params.coinDataItem.balanceCoin / 2,
            fromTransactionPlan = params.transactionPlanItem,
            toAddress = params.coinDataItem.details.walletAddress,
            useMaxAmountFlag = true
        ).map {
            val balance = it.availableAmount ?: params.coinDataItem.balanceCoin
            val additionalFee = if (params.coinDataItem.code == LocalCoinType.XRP.name) 20 else 0
            val max = balance - it.fee - additionalFee
            AmountWithFeeItem(max.coerceAtLeast(0.0), it.fee)
        }

    data class Params(
        val transactionPlanItem: TransactionPlanItem,
        val coinDataItem: CoinDataItem
    )
}