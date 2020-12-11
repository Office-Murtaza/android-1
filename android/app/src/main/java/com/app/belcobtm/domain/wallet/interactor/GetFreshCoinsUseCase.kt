package com.app.belcobtm.domain.wallet.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.wallet.WalletRepository
import com.app.belcobtm.domain.wallet.item.CoinDataItem

class GetFreshCoinsUseCase(private val walletRepository: WalletRepository) :
    UseCase<List<CoinDataItem>, GetFreshCoinsUseCase.Params>() {

    data class Params(val coinCodes: List<String>)

    override suspend fun run(params: Params): Either<Failure, List<CoinDataItem>> {
        return walletRepository.getFreshCoinDataItems(params.coinCodes)
    }
}
