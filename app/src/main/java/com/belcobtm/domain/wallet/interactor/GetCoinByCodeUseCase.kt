package com.belcobtm.domain.wallet.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.wallet.WalletRepository
import com.belcobtm.domain.wallet.item.CoinDataItem

class GetCoinByCodeUseCase(private val walletRepository: WalletRepository) : UseCase<CoinDataItem, String>() {
    override suspend fun run(params: String): Either<Failure, CoinDataItem> =
        walletRepository.getCoinItemByCode(params)
}