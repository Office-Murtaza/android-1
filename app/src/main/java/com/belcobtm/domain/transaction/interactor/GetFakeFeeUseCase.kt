package com.belcobtm.domain.transaction.interactor

import com.belcobtm.domain.*
import com.belcobtm.domain.transaction.TransactionRepository
import com.belcobtm.domain.transaction.item.TransactionPlanItem
import com.belcobtm.domain.wallet.WalletRepository

class GetFakeFeeUseCase(
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository
) : UseCase<Double, GetFakeFeeUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Double> =
        walletRepository.getCoinItemByCode(params.coinCode).flatMapSuspend { coinDataItem ->
            transactionRepository.getFee(
                params.coinCode,
                coinDataItem.reservedBalanceCoin,
                params.transactionPlanItem,
                coinDataItem.details.walletAddress,
            )
        }

    data class Params(
        val coinCode: String,
        val transactionPlanItem: TransactionPlanItem
    )
}