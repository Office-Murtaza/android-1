package com.belcobtm.domain.wallet.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.mapSuspend
import com.belcobtm.domain.wallet.WalletRepository

class UpdateReservedBalanceUseCase(
    private val walletRepository: WalletRepository
) : UseCase<Unit, UpdateReservedBalanceUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Unit> =
        walletRepository.getCoinItemByCode(params.coinCode).mapSuspend { coin ->
            walletRepository.getTotalBalance().mapSuspend { total ->
                if (params.maxAmountUsed) {
                    walletRepository.updateReservedBalance(
                        params.coinCode,
                        newBalance = 0.0,
                        newBalanceUsd = 0.0,
                        newTotal = total - coin.reservedBalanceUsd
                    )
                } else {
                    val txFeeUsd = coin.priceUsd * params.txFee
                    walletRepository.updateReservedBalance(
                        params.coinCode,
                        newBalance = coin.reservedBalanceCoin - params.txCryptoAmount - txFeeUsd,
                        newBalanceUsd = coin.reservedBalanceUsd - params.txAmount - params.txFee,
                        newTotal = total - coin.reservedBalanceUsd
                    )
                }
            }
        }

    data class Params(
        val coinCode: String,
        val txAmount: Double,
        val txCryptoAmount: Double,
        val txFee: Double,
        val maxAmountUsed: Boolean,
    )
}
