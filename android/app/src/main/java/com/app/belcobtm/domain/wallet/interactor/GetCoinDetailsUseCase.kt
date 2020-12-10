package com.app.belcobtm.domain.wallet.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.wallet.WalletRepository
import com.app.belcobtm.domain.wallet.item.CoinDetailsDataItem

class GetCoinDetailsUseCase(private val walletRepository: WalletRepository) :
    UseCase<CoinDetailsDataItem, GetCoinDetailsUseCase.Params>() {

    data class Params(val coinCode: String)

    override suspend fun run(params: Params): Either<Failure, CoinDetailsDataItem> {
        val cachedCoin = walletRepository.getCoinDetailsMap()[params.coinCode]
            ?: return walletRepository.updateCoinDetails(params.coinCode)
        return Either.Right(cachedCoin)
    }
}
