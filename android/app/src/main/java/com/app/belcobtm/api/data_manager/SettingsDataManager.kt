package com.app.belcobtm.api.data_manager

import com.app.belcobtm.api.model.param.CheckPassParam
import com.app.belcobtm.api.model.param.ConfirmPhoneSmsParam
import com.app.belcobtm.api.model.param.UpdatePhoneParam
import com.app.belcobtm.api.model.param.VerifySmsParam
import com.app.belcobtm.api.model.response.*
import com.app.belcobtm.util.Optional
import io.reactivex.Observable

class SettingsDataManager : BaseDataManager() {

    fun checkPass(userId: String, pass: String): Observable<Optional<CheckPassResponse>> {
        return genObservable(api.checkPass(userId, CheckPassParam(pass)))
    }

    fun getPhone(userId: String): Observable<Optional<GetPhoneResponse>> {
        return genObservable(api.getPhone(userId))
    }

    fun updatePhone(userId: String, phone: String): Observable<Optional<UpdatePhoneResponse>> {
        return genObservable(api.updatePhone(userId, UpdatePhoneParam(phone)))
    }

    fun confirmPhoneSms(userId: String, phone: String, smsCode: String): Observable<Optional<ConfirmPhoneSmsResponse>> {
        return genObservable(api.confirmPhoneSms(userId, ConfirmPhoneSmsParam(phone, smsCode)))
    }

}