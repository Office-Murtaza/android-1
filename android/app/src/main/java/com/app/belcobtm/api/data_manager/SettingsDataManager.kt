package com.app.belcobtm.api.data_manager

import com.app.belcobtm.api.model.param.CheckPassParam
import com.app.belcobtm.api.model.response.AtmResponse
import com.app.belcobtm.api.model.response.CheckPassResponse
import com.app.belcobtm.api.model.response.GetCoinsResponse
import com.app.belcobtm.util.Optional
import io.reactivex.Observable

class SettingsDataManager : BaseDataManager() {

    fun checkPass(userId: String, pass: String): Observable<Optional<CheckPassResponse>> {
        return genObservable(api.checkPass(userId, CheckPassParam(pass)))
    }

}