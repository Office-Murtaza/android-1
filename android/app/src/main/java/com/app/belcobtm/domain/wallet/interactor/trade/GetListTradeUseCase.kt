package com.app.belcobtm.domain.wallet.interactor.trade

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.wallet.WalletRepository
import com.app.belcobtm.domain.wallet.item.TradeInfoDataItem
import com.app.belcobtm.domain.wallet.type.TradeSortType

sealed class GetListTradeUseCase : UseCase<TradeInfoDataItem, GetListTradeUseCase.Params>() {

    data class Params(
        val latitude: Double,
        val longitude: Double,
        val coinFrom: String,
        val sortType: TradeSortType,
        val paginationStep: Int
    )

    class Buy(val repository: WalletRepository) : GetListTradeUseCase() {
        override suspend fun run(params: Params): Either<Failure, TradeInfoDataItem> = repository.tradeGetBuyList(
            params.latitude,
            params.longitude,
            params.coinFrom,
            params.sortType,
            params.paginationStep
        )
    }

    class Sell(val repository: WalletRepository) : GetListTradeUseCase() {
        override suspend fun run(params: Params): Either<Failure, TradeInfoDataItem> = repository.getTradeSellList(
            params.latitude,
            params.longitude,
            params.coinFrom,
            params.sortType,
            params.paginationStep
        )
    }

    class My(val repository: WalletRepository) : GetListTradeUseCase() {
        override suspend fun run(params: Params): Either<Failure, TradeInfoDataItem> = repository.getTradeMyList(
            params.latitude,
            params.longitude,
            params.coinFrom,
            params.sortType,
            params.paginationStep
        )
    }

    class Open(val repository: WalletRepository) : GetListTradeUseCase() {
        override suspend fun run(params: Params): Either<Failure, TradeInfoDataItem> = repository.getTradeOpenList(
            params.latitude,
            params.longitude,
            params.coinFrom,
            params.sortType,
            params.paginationStep
        )
    }
}