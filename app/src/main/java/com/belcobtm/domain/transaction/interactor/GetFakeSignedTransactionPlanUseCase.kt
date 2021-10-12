package com.belcobtm.domain.transaction.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.flatMapSuspend
import com.belcobtm.domain.transaction.TransactionRepository
import com.belcobtm.domain.transaction.item.SignedTransactionPlanItem
import com.belcobtm.domain.transaction.item.TransactionPlanItem
import com.belcobtm.domain.wallet.WalletRepository

class GetFakeSignedTransactionPlanUseCase(
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository
) : UseCase<SignedTransactionPlanItem, GetFakeSignedTransactionPlanUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, SignedTransactionPlanItem> =
        walletRepository.getCoinItemByCode(params.coinCode).flatMapSuspend { coinDataItem ->
            transactionRepository.getSignedPlan(
                params.coinCode,
                params.amount ?: coinDataItem.balanceCoin / 2,
                params.transactionPlanItem,
                coinDataItem.details.walletAddress,
                params.useMaxAmount
            )
        }

    data class Params(
        val coinCode: String,
        val transactionPlanItem: TransactionPlanItem,
        val useMaxAmount: Boolean,
        val amount: Double? = null
    )
}