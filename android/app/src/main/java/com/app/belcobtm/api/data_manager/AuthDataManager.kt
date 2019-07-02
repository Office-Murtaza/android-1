package com.app.belcobtm.api.data_manager

import com.app.belcobtm.api.model.param.AddCoinsParam
import com.app.belcobtm.api.model.param.RegisterParam
import com.app.belcobtm.api.model.param.VerifySmsParam
import com.app.belcobtm.api.model.response.AddCoinsResponse
import com.app.belcobtm.api.model.response.RegisterResponse
import com.app.belcobtm.api.model.response.VerifySmsResponse
import com.app.belcobtm.db.CryptoCoin
import com.app.belcobtm.util.Optional
import io.reactivex.Observable

class AuthDataManager : BaseDataManager() {

    fun registerWallet(phone: String, password: String): Observable<Optional<RegisterResponse>> {
        return genObservable(api.register(RegisterParam(phone, password)))
    }

    fun recoverWallet(phone: String, password: String): Observable<Optional<RegisterResponse>> {
        return genObservable(api.register(RegisterParam(phone, password)))
    }

    fun verifySmsCode(smsCode: String, userId: String): Observable<Optional<VerifySmsResponse>> {
        return genObservable(api.verifySmsCode(VerifySmsParam(smsCode, userId)))
    }
    fun addCoins(coins: ArrayList<CryptoCoin>, userId: String): Observable<Optional<AddCoinsResponse>> {
        return genObservable(api.addCoins(AddCoinsParam(coins, userId)))
    }
}