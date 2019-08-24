package com.app.belcobtm.api.data_manager

import com.app.belcobtm.api.model.response.AtmResponse
import com.app.belcobtm.api.model.response.GetCoinsResponse
import com.app.belcobtm.api.model.response.GetTransactionsResponse
import com.app.belcobtm.util.Optional
import io.reactivex.Observable

class CoinsDataManager : BaseDataManager() {

    fun getCoins(userId: String, coins: ArrayList<String>): Observable<Optional<GetCoinsResponse>> {
        return genObservable(api.getCoins(userId, coins))
    }

    fun getAtmAddress(): Observable<Optional<AtmResponse>> {
        return genObservable(api.getAtmAddress())
    }

    fun getTransactions(userId: String, coinId: String, elementIndex: Int): Observable<Optional<GetTransactionsResponse>> {
        return genObservable(api.getTransactions(userId, coinId, elementIndex))
    }

}