package com.app.belcobtm.api.data_manager

import com.app.belcobtm.api.model.param.AddCoinsParam
import com.app.belcobtm.api.model.param.AuthParam
import com.app.belcobtm.api.model.param.RefreshParam
import com.app.belcobtm.api.model.param.VerifySmsParam
import com.app.belcobtm.api.model.response.AddCoinsResponse
import com.app.belcobtm.api.model.response.AuthResponse
import com.app.belcobtm.api.model.response.VerifySmsResponse
import com.app.belcobtm.db.DbCryptoCoin
import com.app.belcobtm.util.Optional
import io.reactivex.Observable

class AuthDataManager : BaseDataManager() {

    fun registerWallet(phone: String, password: String): Observable<Optional<AuthResponse>> {
        return genObservable(api.register(AuthParam(phone, password)))
    }

    fun recoverWallet(phone: String, password: String): Observable<Optional<AuthResponse>> {
        return genObservable(api.recover(AuthParam(phone, password)))
    }

    fun refreshToken(refreshToken: String?): Observable<Optional<AuthResponse>> {
        return genObservable(api.refresh(RefreshParam(refreshToken)))
    }

    fun login(phone: String, password: String): Observable<Optional<AuthResponse>> {
        return genObservable(api.login(AuthParam(phone, password)))
    }

    fun verifySmsCode(userId: String, smsCode: String): Observable<Optional<VerifySmsResponse>> {
        return genObservable(api.verifySmsCode(userId, VerifySmsParam(smsCode)))
    }

    fun addCoins(
        userId: String,
        coinDbs: ArrayList<DbCryptoCoin>
    ): Observable<Optional<AddCoinsResponse>> {
        return genObservable(api.addCoins(userId, AddCoinsParam(coinDbs)))
    }

    fun verifyCoins(
        userId: String,
        coinDbs: ArrayList<DbCryptoCoin>
    ): Observable<Optional<AddCoinsResponse>> {
        return genObservable(api.verifyCoins(userId, AddCoinsParam(coinDbs)))
    }
}