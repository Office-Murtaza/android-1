package com.app.belcobtm.domain.wallet.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.wallet.WalletRepository
import com.app.belcobtm.domain.wallet.item.TradeInfoDataItem

class GetTradeInfoUseCase(private val repository: WalletRepository) :
    UseCase<TradeInfoDataItem, GetTradeInfoUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, TradeInfoDataItem> =
        repository.getTradeInformation(params.latitude, params.longitude)

    data class Params(val latitude: Double, val longitude: Double)
}