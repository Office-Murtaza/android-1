package com.app.belcobtm.domain.wallet.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.wallet.WalletRepository
import com.app.belcobtm.domain.wallet.item.ChartDataItem

class GetChartsUseCase(private val repository: WalletRepository) : UseCase<ChartDataItem, GetChartsUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, ChartDataItem> = repository.getChart(params.coinCode)

    data class Params(val coinCode: String)
}