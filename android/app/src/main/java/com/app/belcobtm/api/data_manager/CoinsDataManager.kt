package com.app.belcobtm.api.data_manager

import com.app.belcobtm.api.model.response.*
import com.app.belcobtm.presentation.core.Optional
import io.reactivex.Observable

class CoinsDataManager : BaseDataManager() {

    fun getCoins(userId: String, coins: ArrayList<String>): Observable<Optional<GetCoinsResponse>> {
        return genObservable(api.getCoins(userId, coins))
    }

    fun getCoinsFee(userId: String): Observable<Optional<GetCoinsFeeOldResponse>> {
        return genObservable(api.getCoinsFee(userId))
    }

    fun getCoinFee(coinId: String): Observable<Optional<GetCoinFeeResponse>> {
        return genObservable(api.getCoinFee(coinId))
    }

    fun getAtmAddress(): Observable<Optional<AtmResponse>> {
        return genObservable(api.getAtmAddress())
    }

    fun getTransactions(
        userId: String,
        coinId: String,
        elementIndex: Int
    ): Observable<Optional<GetTransactionsResponse>> {
        return genObservable(api.getTransactions(userId, coinId, elementIndex))
    }

    fun getChart(userId: String, coinId: String): Observable<Optional<ChartResponse>> {
        return genObservable(api.getChartAsync(userId, coinId))
    }

}