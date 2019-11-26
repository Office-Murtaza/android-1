package com.app.belcobtm.api.data_manager

import com.app.belcobtm.api.RetrofitClient
import com.app.belcobtm.api.model.ServerException
import com.app.belcobtm.api.model.ServerResponse
import com.app.belcobtm.util.None
import com.app.belcobtm.util.Optional
import com.app.belcobtm.util.Some
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

open class BaseDataManager {
    val api = RetrofitClient.instance.apiInterface

    fun updateToken() {
        RetrofitClient.instance.updateToken()
    }

    protected fun <T> genObservable(observable: Observable<ServerResponse<T>>): Observable<Optional<T>> {
        return applySchedulers(observable)
            .flatMap { response ->
                when {
                    response.error != null -> Observable.error(
                        ServerException(
                            response.error.errorCode,
                            response.error.errorMsg
                        )
                    )
                    response.response == null -> Observable.just(None)
                    else -> Observable.just(Some(response.response))
                }
            }
    }

    private fun <T> applySchedulers(observable: Observable<ServerResponse<T>>): Observable<ServerResponse<T>> {
        return observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }
}