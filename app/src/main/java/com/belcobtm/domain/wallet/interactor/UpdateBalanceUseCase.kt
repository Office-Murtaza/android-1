package com.belcobtm.domain.wallet.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.mapSuspend
import com.belcobtm.domain.wallet.WalletRepository

class UpdateBalanceUseCase(
    private val walletRepository: WalletRepository
) : UseCase<Unit, UpdateBalanceUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Unit> =
        walletRepository.getCoinItemByCode(params.coinCode).mapSuspend { coin ->
            walletRepository.getTotalBalance().mapSuspend { total ->
                if (params.maxAmountUsed) {
                    walletRepository.updateBalance(
                        params.coinCode,
                        newBalance = 0.0,
                        newBalanceUsd = 0.0,
                        newTotal = total - coin.balanceUsd
                    )
                } else {
                    val txFeeUsd = coin.priceUsd * params.txFee
                    walletRepository.updateBalance(
                        params.coinCode,
                        newBalance = coin.balanceCoin - params.txCryptoAmount - params.txFee,
                        newBalanceUsd = coin.balanceUsd - params.txAmount - txFeeUsd,
                        newTotal = total - coin.balanceUsd - txFeeUsd
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
