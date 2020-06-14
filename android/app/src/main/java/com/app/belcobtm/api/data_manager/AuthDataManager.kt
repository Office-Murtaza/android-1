package com.app.belcobtm.api.data_manager

import com.app.belcobtm.api.model.param.AddCoinsParam
import com.app.belcobtm.api.model.response.AddCoinsResponse
import com.app.belcobtm.domain.wallet.item.LocalCoinDataItem
import com.app.belcobtm.presentation.core.Optional
import io.reactivex.Observable

class AuthDataManager : BaseDataManager() {

    fun verifyCoins(
        userId: String,
        localCoinDbs: List<LocalCoinDataItem>
    ): Observable<Optional<AddCoinsResponse>> {
        return genObservable(api.verifyCoins(userId, AddCoinsParam(localCoinDbs)))
    }
}