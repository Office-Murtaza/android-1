package com.belcobtm.domain.wallet.interactor

import com.belcobtm.data.rest.wallet.request.PriceChartPeriod
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.wallet.WalletRepository
import com.belcobtm.domain.wallet.item.ChartDataItem

class GetChartsUseCase(
    private val repository: WalletRepository
) : UseCase<ChartDataItem, GetChartsUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, ChartDataItem> =
        repository.getChart(params.coinCode, params.period)

    data class Params(val coinCode: String, @PriceChartPeriod val period: Int)
}