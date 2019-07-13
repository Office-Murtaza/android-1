package com.app.belcobtm.api.data_manager

import com.app.belcobtm.api.model.response.GetCoinsResponse
import com.app.belcobtm.util.Optional
import io.reactivex.Observable

class CoinsDataManager : BaseDataManager() {

    fun getCoins(userId: String): Observable<Optional<GetCoinsResponse>> {
        return genObservable(api.getCoins(userId))
    }

}