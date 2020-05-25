package com.app.belcobtm.api.data_manager

import com.app.belcobtm.api.model.param.AddCoinsParam
import com.app.belcobtm.api.model.param.RefreshParam
import com.app.belcobtm.api.model.param.VerifySmsParam
import com.app.belcobtm.api.model.response.AddCoinsResponse
import com.app.belcobtm.api.model.response.AuthResponse
import com.app.belcobtm.api.model.response.VerifySmsResponse
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.presentation.core.Optional
import io.reactivex.Observable

class AuthDataManager : BaseDataManager() {

    fun verifyCoins(
        userId: String,
        coinDbs: List<CoinDataItem>
    ): Observable<Optional<AddCoinsResponse>> {
        return genObservable(api.verifyCoins(userId, AddCoinsParam(coinDbs)))
    }
}