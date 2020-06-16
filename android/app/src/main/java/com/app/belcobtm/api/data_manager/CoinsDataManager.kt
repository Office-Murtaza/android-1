package com.app.belcobtm.api.data_manager

import com.app.belcobtm.api.model.response.AtmResponse
import com.app.belcobtm.presentation.core.Optional
import io.reactivex.Observable

class CoinsDataManager : BaseDataManager() {
    fun getAtmAddress(): Observable<Optional<AtmResponse>> {
        return genObservable(api.getAtmAddress())
    }
}