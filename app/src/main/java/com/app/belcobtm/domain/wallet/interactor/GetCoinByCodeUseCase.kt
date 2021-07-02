package com.app.belcobtm.domain.wallet.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.wallet.WalletRepository
import com.app.belcobtm.domain.wallet.item.CoinDataItem

class GetCoinByCodeUseCase(private val walletRepository: WalletRepository) : UseCase<CoinDataItem, String>() {
    override suspend fun run(params: String): Either<Failure, CoinDataItem> =
        walletRepository.getCoinItemByCode(params)
}